/** MethodAnalysisToJson.java (batch by CSV project+vid) - Java 8 compatible */

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import soot.*;
import soot.jimple.IfStmt;
import soot.jimple.LookupSwitchStmt;
import soot.jimple.TableSwitchStmt;
import soot.options.Options;
import soot.toolkits.graph.Block;
import soot.toolkits.graph.BlockGraph;
import soot.toolkits.graph.BriefBlockGraph;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import java.util.stream.Collectors;
import java.util.stream.Stream;

class DependencyInfo {
    String name;
    String body;
}

class MethodInfo {
    String clazz;
    String methodName;
    String signature;
    String visibility;
    String body;
    int nodes;
    int edges;
    int cc;
    List<String> flowSummary;
    List<String> blockList;
    List<String> blockEdges;
    List<DependencyInfo> depClasses  = new ArrayList<DependencyInfo>();
    List<DependencyInfo> depMethods  = new ArrayList<DependencyInfo>();
}

public class MethodAnalysisToJson {

    /* ================================
     * CLI 사용:
     *   mvn -DskipTests exec:java \
     *     -Dexec.mainClass=MethodAnalysisToJson \
     *     -Dexec.args="--root /workspace --csv /workspace/method_output.csv --out /workspace/method-json --skipExisting true"
     * ================================ */

    static class Pair {
        final String project;
        final String vid;
        Pair(String p, String v) { project = p; vid = v; }

        @Override public boolean equals(Object o) {
            if (!(o instanceof Pair)) return false;
            Pair other = (Pair) o;
            return Objects.equals(project, other.project) && Objects.equals(vid, other.vid);
        }
        @Override public int hashCode() { return Objects.hash(project, vid); }
        @Override public String toString() { return project + "," + vid; }
    }

    public static void main(String[] args) throws Exception {
        Map<String,String> cli = parseArgs(args);

        Path root = Paths.get(cli.getOrDefault("--root", ".")).toAbsolutePath().normalize();
        Path csv  = Paths.get(cli.getOrDefault("--csv", root.resolve("method_output.csv").toString()))
                .toAbsolutePath().normalize();
        Path outDir = Paths.get(cli.getOrDefault("--out", root.resolve("method-json").toString()))
                .toAbsolutePath().normalize();

        boolean skipExisting = Boolean.parseBoolean(cli.getOrDefault("--skipExisting", "true"));

        if (!Files.exists(csv)) die("CSV not found: " + csv);
        Files.createDirectories(outDir);

        System.out.println("ROOT=" + root);
        System.out.println("CSV=" + csv);
        System.out.println("OUT=" + outDir);
        System.out.println("skipExisting=" + skipExisting);
        System.out.println();

        LinkedHashSet<Pair> tasks = readProjectVidPairs(csv);
        System.out.println("Unique (project,vid) tasks = " + tasks.size());
        System.out.println();

        int ok = 0, fail = 0, skip = 0;

        for (Pair t : tasks) {
            String bugNum = vidToBugnum(t.vid);
            Path projectDir = root.resolve("buggy").resolve(t.project + "-" + bugNum + "");

            Path projectOutDir = outDir.resolve(t.project).resolve(t.vid);
            Files.createDirectories(projectOutDir);
            Path jsonOut = projectOutDir.resolve("methods.json");

            System.out.println("==================================================");
            System.out.println("PROJECT=" + t.project + " VID=" + t.vid + " BUGNUM=" + bugNum);
            System.out.println("PROJECT_DIR=" + projectDir);
            System.out.println("JSON_OUT=" + jsonOut);

            if (!Files.isDirectory(projectDir)) {
                System.out.println("  -> FAIL: project dir not found");
                fail++;
                continue;
            }

            if (skipExisting && Files.exists(jsonOut) && Files.size(jsonOut) > 0) {
                System.out.println("  -> SKIP: output already exists");
                skip++;
                continue;
            }

            try {
                analyzeOneProject(projectDir, jsonOut);
                ok++;
            } catch (Throwable e) {
                System.out.println("  -> FAIL: " + e.getClass().getSimpleName() + " : " + e.getMessage());
                fail++;
            }
        }

        System.out.println();
        System.out.println("DONE. ok=" + ok + " skip=" + skip + " fail=" + fail);
        if (fail > 0) System.exit(1);
    }

    /* ========= CSV: project+vid 중복 제거 ========= */
    private static LinkedHashSet<Pair> readProjectVidPairs(Path csv) throws IOException {
        List<String> lines = Files.readAllLines(csv, StandardCharsets.UTF_8);
        if (lines.isEmpty()) die("CSV is empty: " + csv);

        // 아주 단순 CSV(콤마 포함 필드 없다고 가정)
        String header = lines.get(0);
        String[] hs = splitCsvLine(header);
        int pIdx = -1, vIdx = -1;

        for (int i = 0; i < hs.length; i++) {
            String h = hs[i].trim().replace("\"","").toLowerCase();
            if (h.equals("project")) pIdx = i;
            if (h.equals("vid")) vIdx = i;
        }
        if (pIdx < 0 || vIdx < 0) die("CSV header must include project, vid");

        LinkedHashSet<Pair> set = new LinkedHashSet<Pair>();
        for (int r = 1; r < lines.size(); r++) {
            String line = lines.get(r).trim();
            if (line.isEmpty()) continue;
            String[] fs = splitCsvLine(line);
            if (fs.length <= Math.max(pIdx, vIdx)) continue;

            String project = dequote(fs[pIdx]).trim();
            String vid = dequote(fs[vIdx]).trim();

            if (project.isEmpty() || vid.isEmpty()) continue;
            set.add(new Pair(project, vid));
        }
        return set;
    }

    private static String[] splitCsvLine(String line) {
        return line.split(",", -1);
    }

    private static String dequote(String s) {
        s = s.trim();
        if (s.startsWith("\"") && s.endsWith("\"") && s.length() >= 2) {
            return s.substring(1, s.length()-1);
        }
        return s;
    }

    /* ========= 프로젝트 1개 분석 ========= */
    private static void analyzeOneProject(Path projectDir, Path jsonOut) throws Exception {

        // (1) class dirs 찾기: 멀티모듈 대응 (target/classes 여러개 가능)
        List<Path> classDirs = findClassDirs(projectDir);
        if (classDirs.isEmpty()) die("No target/classes found under: " + projectDir);

        // (2) source roots 찾기: src/main/java 여러개 가능
        List<Path> sourceRoots = findSourceRoots(projectDir);
        if (sourceRoots.isEmpty()) {
            // 최후: 전체 프로젝트에서 java 파일 찾도록 projectDir 자체를 source root로 둠
            sourceRoots = Collections.singletonList(projectDir);
        }

        System.out.println("  classDirs=" + classDirs.size());
        System.out.println("  sourceRoots=" + sourceRoots.size());

        // 소스 인덱스: simpleName -> Path
        Map<String, Path> sourceIndex = buildSourceIndex(sourceRoots);

        // (3) Soot 초기화 (프로젝트마다 reset)
        G.reset();
        Options.v().set_prepend_classpath(true);
        Options.v().set_allow_phantom_refs(true);
        Options.v().set_output_format(Options.output_format_none);

        Options.v().set_process_dir(classDirs.stream().map(Path::toString).collect(Collectors.toList()));

        // soot classpath: classDirs + 현재 java classpath
        String sootCp = String.join(
                File.pathSeparator,
                classDirs.stream().map(Path::toString).collect(Collectors.toList())
        ) + File.pathSeparator + System.getProperty("java.class.path");

        Options.v().set_soot_classpath(sootCp);
        Scene.v().loadNecessaryClasses();

        List<MethodInfo> result = new ArrayList<MethodInfo>();
        Map<String, CompilationUnit> cuCache = new HashMap<String, CompilationUnit>();

        // (4) Soot 순회
        for (SootClass sc : new ArrayList<SootClass>(Scene.v().getApplicationClasses())) {
            for (SootMethod sm : new ArrayList<SootMethod>(sc.getMethods())) {

                if (!isEligible(sc, sm)) continue;

                Body body;
                try { body = sm.retrieveActiveBody(); }
                catch (Exception ex) { continue; }

                BlockGraph cfg = new BriefBlockGraph(body);
                int nodes = cfg.size();
                int edges = 0;
                for (Block b : cfg.getBlocks()) edges += cfg.getSuccsOf(b).size();
                int cc    = edges - nodes + 2;

                if (nodes == 0 || edges == 0) continue;
                if (cc <= 0) continue;

                int startLine = Integer.MAX_VALUE;
                int endLine = Integer.MIN_VALUE;
                for (Unit unit : body.getUnits()) {
                    int line = unit.getJavaSourceStartLineNumber();
                    if (line > 0) {
                        startLine = Math.min(startLine, line);
                        endLine = Math.max(endLine, line);
                    }
                }

                // flow summary
                List<String> flow = new ArrayList<String>();
                for (Block blk : cfg) {
                    List<Block> succs = cfg.getSuccsOf(blk);
                    if (succs.size() < 2) continue;
                    Unit tail = blk.getTail();
                    String cond;
                    if (tail instanceof IfStmt)
                        cond = ((IfStmt) tail).getCondition().toString();
                    else if (tail instanceof LookupSwitchStmt || tail instanceof TableSwitchStmt)
                        cond = "switch-on " + tail.getUseBoxes().get(0).getValue();
                    else
                        continue;

                    flow.add(String.format("B%d : If(%s) -> B%d | else -> B%d",
                            blk.getIndexInMethod(), cond,
                            succs.get(0).getIndexInMethod(), succs.get(1).getIndexInMethod()));
                }

                // JavaParser로 소스 찾기
                String fqn = sc.getName();
                Path srcPath = findSourcePathByFqn(sourceRoots, fqn);
                if (srcPath == null) {
                    srcPath = sourceIndex.get(sc.getShortName());
                }

                CompilationUnit cu = loadCU(srcPath, cuCache);

                String bodySrc = "(source not found)";
                if (cu != null) {
                    Optional<ClassOrInterfaceDeclaration> cd = cu.getClassByName(sc.getShortName());
                    if (cd.isPresent()) {
                        Optional<MethodDeclaration> md = cd.get().getMethods().stream()
                                .filter(m -> matches(m, sm))
                                .findFirst();
                        if (md.isPresent()) {
                            bodySrc = md.get().getBody().map(Object::toString).orElse("(no body)");
                        }
                    }
                }

                // fallback: start/end line 기반
                if ("(source not found)".equals(bodySrc) && srcPath != null && startLine < endLine) {
                    try {
                        List<String> lines = Files.readAllLines(srcPath, StandardCharsets.UTF_8);
                        bodySrc = String.join(System.lineSeparator(),
                                lines.subList(startLine - 1, Math.min(endLine, lines.size())));
                    } catch (Exception ignore) {}
                }

                Map<String,List<String>> pretty = buildPrettyCFG(body);

                // 의존성 분석
                Set<String> classDeps = new HashSet<String>();
                Set<List<String>> selfMethodDeps = new HashSet<List<String>>();
                ClassOrInterfaceDeclaration cdAst = null;

                if (cu != null) {
                    Optional<ClassOrInterfaceDeclaration> optCd = cu.getClassByName(sc.getShortName());
                    if (optCd.isPresent()) {
                        cdAst = optCd.get();

                        Optional<MethodDeclaration> optMd = cdAst.getMethods().stream()
                                .filter(new java.util.function.Predicate<MethodDeclaration>() {
                                    @Override
                                    public boolean test(MethodDeclaration m) {
                                        return matches(m, sm);
                                    }
                                })
                                .findFirst();

                        if (optMd.isPresent()) {
                            MethodDeclaration md = optMd.get();
                            String pkg = sc.getPackageName();
                            classDeps.addAll(findInternalClassDeps(md, pkg));
                            selfMethodDeps.addAll(findSelfMethodDeps(md));
                        }
                    }
                }

                MethodInfo mi = new MethodInfo();

                // depClasses
                for (String clsSimpleOrFqn : classDeps) {
                    String simple = clsSimpleOrFqn.contains(".")
                            ? clsSimpleOrFqn.substring(clsSimpleOrFqn.lastIndexOf('.')+1)
                            : clsSimpleOrFqn;

                    String fqnDep = clsSimpleOrFqn.contains(".")
                            ? clsSimpleOrFqn
                            : sc.getPackageName() + "." + simple;

                    Path depSrc = findSourcePathByFqn(sourceRoots, fqnDep);
                    if (depSrc == null) depSrc = sourceIndex.get(simple);

                    DependencyInfo di = new DependencyInfo();
                    di.name = fqnDep;
                    CompilationUnit depCu = loadCU(depSrc, cuCache);
                    di.body = depCu != null ? depCu.toString() : "(source not found)";
                    mi.depClasses.add(di);
                }

                // depMethods (self calls)
                for (List<String> sig : selfMethodDeps) {
                    String mName = sig.get(0);
                    int arity = Integer.parseInt(sig.get(1));

                    DependencyInfo di = new DependencyInfo();
                    di.name = sc.getShortName() + "#" + mName + "(..." + arity + ")";

                    String bodyTxt = "(source not found)";
                    if (cdAst != null) {
                        bodyTxt = findMethodBodySrc(cdAst, mName, Collections.nCopies(arity, ""));
                    }
                    if ("(source not found)".equals(bodyTxt)) {
                        Path clsPath = sourceIndex.get(sc.getShortName());
                        CompilationUnit altCu = loadCU(clsPath, cuCache);
                        if (altCu != null) {
                            Optional<ClassOrInterfaceDeclaration> altCd =
                                    altCu.getClassByName(sc.getShortName());
                            if (altCd.isPresent()) {
                                bodyTxt = findMethodBodySrc(altCd.get(), mName, Collections.nCopies(arity, ""));
                            }
                        }
                    }
                    di.body = bodyTxt;
                    mi.depMethods.add(di);
                }

                // main fields
                mi.clazz       = fqn;
                mi.methodName  = sm.getName();
                mi.signature   = sm.getSubSignature();
                mi.visibility  = vis(sm);
                mi.body        = bodySrc;
                mi.nodes       = nodes;
                mi.edges       = edges;
                mi.cc          = cc;
                mi.flowSummary = flow;
                mi.blockList   = pretty.get("blocks");
                mi.blockEdges  = pretty.get("edges");

                result.add(mi);
            }
        }

        // (5) JSON 저장
        Gson gson = new GsonBuilder().setPrettyPrinting().create();
        Files.createDirectories(jsonOut.getParent());
        try (Writer w = Files.newBufferedWriter(jsonOut, StandardCharsets.UTF_8)) {
            gson.toJson(result, w);
        }

        System.out.println("  -> OK: saved " + result.size() + " methods");
    }

    /* ========= 경로 탐색 ========= */
    private static List<Path> findClassDirs(Path projectDir) throws IOException {
        // target/classes 모두 수집
        try (Stream<Path> s = Files.walk(projectDir, 6)) {
            return s.filter(Files::isDirectory)
                    .filter(p -> p.endsWith(Paths.get("target", "classes")))
                    .collect(Collectors.toList());
        }
    }

    private static List<Path> findSourceRoots(Path projectDir) throws IOException {
        List<Path> roots = new ArrayList<Path>();

        try (Stream<Path> s = Files.walk(projectDir, 8)) {
            List<Path> found = s
                    .filter(Files::isDirectory)
                    .filter(p -> p.endsWith(Paths.get("src", "main", "java"))
                            || p.endsWith(Paths.get("src", "main")))
                    .collect(Collectors.toList());
            roots.addAll(found);
        }

        boolean hasMainJava = false;
        for (Path p : roots) {
            if (p.endsWith(Paths.get("src","main","java"))) { hasMainJava = true; break; }
        }

        if (hasMainJava) {
            List<Path> only = new ArrayList<Path>();
            for (Path p : roots) {
                if (p.endsWith(Paths.get("src","main","java"))) only.add(p);
            }
            roots = only; // 재할당 OK (이제 람다에서 roots를 캡처 안 함)
        }

        return new ArrayList<Path>(new LinkedHashSet<Path>(roots));
    }

    private static Map<String, Path> buildSourceIndex(List<Path> sourceRoots) throws IOException {
        Map<String, Path> idx = new HashMap<String, Path>();
        for (Path r : sourceRoots) {
            try (Stream<Path> s = Files.walk(r)) {
                s.filter(p -> p.toString().endsWith(".java"))
                        .forEach(p -> {
                            String simple = p.getFileName().toString().replace(".java", "");
                            if (!idx.containsKey(simple)) idx.put(simple, p);
                        });
            }
        }
        return idx;
    }

    private static Path findSourcePathByFqn(List<Path> sourceRoots, String fqn) {
        String suffix = fqn.replace('.', File.separatorChar) + ".java";
        for (Path r : sourceRoots) {
            Path p = r.resolve(suffix);
            if (Files.exists(p)) return p;
        }
        return null;
    }

    /* ========= 기존 유틸/로직 ========= */

    private static boolean isEligible(SootClass c, SootMethod m) {
        if (!c.isPublic() || c.isInterface() || c.getName().contains("$")) return false;
        if (!m.isConcrete()) return false;
        if (m.isConstructor() || m.isStaticInitializer()) return false;

        final int ACC_BRIDGE    = 0x0040;
        final int ACC_SYNTHETIC = 0x1000;
        int mod = m.getModifiers();
        if (!Modifier.isPublic(mod)) return false;
        return (mod & ACC_BRIDGE) == 0 && (mod & ACC_SYNTHETIC) == 0;
    }

    private static List<String> sootParts(String sub) {
        int sp = sub.indexOf(' ');
        String rest = sub.substring(sp + 1);
        String name = rest.substring(0, rest.indexOf('('));
        String[] ps = rest.substring(rest.indexOf('(') + 1, rest.lastIndexOf(')')).split(",");

        List<String> out = new ArrayList<String>();
        out.add(name);
        for (String p : ps) {
            if (p != null && !p.trim().isEmpty()) out.add(norm(p));
        }
        return out;
    }

    private static boolean matches(MethodDeclaration src, SootMethod sm) {
        if (!src.getNameAsString().equals(sm.getName())) return false;
        if (src.getParameters().size() != sm.getParameterCount()) return false;

        List<String> soot = sootParts(sm.getSubSignature());
        for (int i = 0; i < src.getParameters().size(); i++) {
            String srcTy  = norm(src.getParameter(i).getType().asString());
            String sootTy = soot.get(i + 1);
            if (!srcTy.equalsIgnoreCase(sootTy)) return false;
        }
        return true;
    }

    private static String norm(String t) {
        t = t.trim();
        while (t.endsWith("[]") || t.endsWith("...")) {
            int lb = t.lastIndexOf('[');
            int dot = t.lastIndexOf('.');
            int cut = (lb > 0 ? lb : dot);
            if (cut <= 0) break;
            t = t.substring(0, cut);
        }
        int g = t.indexOf('<');
        if (g >= 0) t = t.substring(0, g);
        int p = t.lastIndexOf('.');
        return p >= 0 ? t.substring(p + 1) : t;
    }

    private static Map<String, List<String>> buildPrettyCFG(Body body) {
        BlockGraph cfg = new BriefBlockGraph(body);

        Map<Unit,Integer> u2b = new HashMap<Unit, Integer>();
        for (Block b : cfg) for (Unit u : b) u2b.put(u, b.getIndexInMethod());

        List<String> blocks = new ArrayList<String>();
        List<String> edges  = new ArrayList<String>();

        for (Block b : cfg) {
            StringBuilder sb = new StringBuilder();
            sb.append("B").append(b.getIndexInMethod()).append(" {");

            for (Unit u : b) {
                String out = pseudo(u, b, cfg, u2b);
                sb.append("\n  ").append(out);
            }
            sb.append("\n}");
            blocks.add(sb.toString());

            if (cfg.getSuccsOf(b).isEmpty()) {
                edges.add("B"+b.getIndexInMethod()+" --> [EXIT]");
            } else {
                for (Block s : cfg.getSuccsOf(b)) {
                    edges.add("B"+b.getIndexInMethod()+" --> B"+s.getIndexInMethod());
                }
            }
        }
        Map<String,List<String>> m = new HashMap<String, List<String>>();
        m.put("blocks", blocks);
        m.put("edges",  edges);
        return m;
    }

    private static String invoke2pseudo(soot.jimple.InvokeExpr ie) {
        // constructor
        if ((ie instanceof soot.jimple.SpecialInvokeExpr) &&
                ie.getMethodRef().name().equals("<init>")) {
            soot.jimple.SpecialInvokeExpr sie = (soot.jimple.SpecialInvokeExpr) ie;
            String cls = sie.getBase().getType().toString();
            cls = cls.substring(cls.lastIndexOf('.') + 1);
            String args = argList(ie);
            return "new " + cls + "(" + args + ")";
        }

        // static invoke
        if (ie instanceof soot.jimple.StaticInvokeExpr) {
            String cls = ie.getMethodRef().declaringClass().getName();
            cls = cls.substring(cls.lastIndexOf('.') + 1);
            return cls + "." + ie.getMethodRef().name() + "(" + argList(ie) + ")";
        }

        // instance invoke
        if (ie instanceof soot.jimple.InstanceInvokeExpr) {
            soot.jimple.InstanceInvokeExpr iie = (soot.jimple.InstanceInvokeExpr) ie;
            String base = strip(iie.getBase().toString());
            return base + "." + ie.getMethodRef().name() + "(" + argList(ie) + ")";
        }

        return "dynInvoke " + ie.getMethodRef().name() + "(" + argList(ie) + ")";
    }

    private static String argList(soot.jimple.InvokeExpr ie) {
        return ie.getArgs().isEmpty()
                ? ""
                : ie.getArgs().toString().replace("[", "").replace("]", "");
    }

    private static String strip(String s) {
        if (s.startsWith("@this:"))       return "this";
        if (s.startsWith("@parameter"))   return "param" + s.charAt(10);
        int p = s.lastIndexOf('.');
        return p >= 0 ? s.substring(p+1) : s;
    }

    private static String pseudo(Unit u, Block curBlk, BlockGraph cfg, Map<Unit,Integer> unit2Block) {
        if (u instanceof soot.jimple.AssignStmt) {
            soot.jimple.AssignStmt as = (soot.jimple.AssignStmt) u;
            String lhs = strip(as.getLeftOp().toString());
            String rhs = as.containsInvokeExpr()
                    ? invoke2pseudo(as.getInvokeExpr())
                    : strip(as.getRightOp().toString());
            return lhs + " = " + rhs;
        }
        if (u instanceof soot.jimple.IfStmt) {
            soot.jimple.IfStmt is = (soot.jimple.IfStmt) u;
            List<Block> succs = cfg.getSuccsOf(curBlk);
            if (succs.size() == 2) {
                int trueBid  = succs.get(0).getIndexInMethod();
                int falseBid = succs.get(1).getIndexInMethod();
                return String.format("if (%s) goto B%d else B%d",
                        strip(is.getCondition().toString()), trueBid, falseBid);
            }
            return "if (" + strip(is.getCondition().toString()) + ") ...";
        }
        if (u instanceof soot.jimple.GotoStmt) {
            soot.jimple.GotoStmt gs = (soot.jimple.GotoStmt) u;
            Integer bid = unit2Block.get(gs.getTarget());
            return "goto B" + (bid == null ? "?" : bid);
        }
        if (u instanceof soot.jimple.ReturnStmt || u instanceof soot.jimple.ReturnVoidStmt) {
            return "return";
        }
        if (u instanceof soot.jimple.ThrowStmt) {
            soot.jimple.ThrowStmt ts = (soot.jimple.ThrowStmt) u;
            return "throw " + strip(ts.getOp().toString());
        }
        if (u instanceof soot.jimple.InvokeStmt) {
            soot.jimple.InvokeStmt ivs = (soot.jimple.InvokeStmt) u;
            return invoke2pseudo(ivs.getInvokeExpr());
        }
        return strip(u.toString());
    }

    private static String vis(SootMethod m) {
        if (m.isPublic())    return "public";
        if (m.isProtected()) return "protected";
        if (m.isPrivate())   return "private";
        return "package";
    }

    private static Set<String> findInternalClassDeps(MethodDeclaration md, String pkg) {
        Set<String> set = new HashSet<String>();
        md.findAll(com.github.javaparser.ast.type.ClassOrInterfaceType.class)
                .stream()
                .map(t -> t.getNameWithScope())
                .filter(n -> n.contains(".") && n.startsWith(pkg))
                .forEach(set::add);
        return set;
    }

    private static Set<List<String>> findSelfMethodDeps(MethodDeclaration md) {
        Set<List<String>> deps = new HashSet<List<String>>();
        md.findAll(com.github.javaparser.ast.expr.MethodCallExpr.class)
                .stream()
                .filter(mc -> !mc.getScope().isPresent() || mc.getScope().get().isThisExpr())
                .forEach(mc -> deps.add(Arrays.asList(mc.getNameAsString(), String.valueOf(mc.getArguments().size()))));
        return deps;
    }

    private static String findMethodBodySrc(ClassOrInterfaceDeclaration cd, String mName, List<String> dummyParamList) {
        int arity = dummyParamList.size();
        return cd.getMethods().stream()
                .filter(m -> m.getNameAsString().equals(mName) && m.getParameters().size() == arity)
                .findFirst()
                .flatMap(m -> m.getBody().map(Object::toString))
                .orElse("(source not found)");
    }

    private static CompilationUnit loadCU(final Path p, Map<String, CompilationUnit> cuCache) {
        if (p == null) return null;
        final String key = p.toAbsolutePath().toString();
        CompilationUnit cached = cuCache.get(key);
        if (cached != null) return cached;

        CompilationUnit parsed = null;
        try {
            byte[] bytes = Files.readAllBytes(p);
            String text = new String(bytes, StandardCharsets.UTF_8);
            parsed = StaticJavaParser.parse(text);
        } catch (Exception e) {
            parsed = null;
        }
        cuCache.put(key, parsed);
        return parsed;
    }

    private static String vidToBugnum(String vid) {
        String num = vid.replaceAll("^([0-9]+).*$", "$1");
        if (num == null || num.trim().isEmpty() || !num.matches("\\d+")) {
            die("Cannot parse bug number from vid: " + vid);
        }
        return num;
    }

    private static Map<String,String> parseArgs(String[] args) {
        Map<String,String> m = new HashMap<String, String>();
        for (int i = 0; i < args.length; i++) {
            String a = args[i];
            if (a.startsWith("--")) {
                String v = "true";
                if (i + 1 < args.length && !args[i+1].startsWith("--")) v = args[++i];
                m.put(a, v);
            }
        }
        return m;
    }

    private static void die(String msg) {
        System.err.println("ERROR: " + msg);
        System.exit(1);
    }
}