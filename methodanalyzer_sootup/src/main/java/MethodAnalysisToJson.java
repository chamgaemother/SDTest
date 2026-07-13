/** MethodAnalysisToJson.java (batch by CSV project+vid) - SootUp version */

import com.github.javaparser.StaticJavaParser;
import com.github.javaparser.ast.CompilationUnit;
import com.github.javaparser.ast.body.ClassOrInterfaceDeclaration;
import com.github.javaparser.ast.body.MethodDeclaration;
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;

import sootup.core.graph.BasicBlock;
import sootup.core.graph.StmtGraph;
import sootup.core.inputlocation.AnalysisInputLocation;
import sootup.core.jimple.common.stmt.JIfStmt;
import sootup.core.jimple.common.stmt.Stmt;
import sootup.core.model.MethodModifier;
import sootup.core.model.SootClass;
import sootup.core.model.SootMethod;

import sootup.java.bytecode.frontend.inputlocation.JavaClassPathAnalysisInputLocation;
import sootup.java.bytecode.frontend.inputlocation.JrtFileSystemAnalysisInputLocation;
import sootup.java.core.JavaSootClass;
import sootup.java.core.views.JavaView;

import java.io.File;
import java.io.IOException;
import java.io.Writer;
import java.nio.charset.StandardCharsets;
import java.nio.file.*;
import java.util.*;
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
            Path projectDir = root.resolve("buggy").resolve(t.project + "-" + bugNum);

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

        // (1) class dirs 찾기 (target/classes)
        List<Path> classDirs = findClassDirs(projectDir);
        if (classDirs.isEmpty()) die("No target/classes found under: " + projectDir);

        // (2) source roots 찾기
        List<Path> sourceRoots = findSourceRoots(projectDir);
        if (sourceRoots.isEmpty()) sourceRoots = Collections.singletonList(projectDir);

        System.out.println("  classDirs=" + classDirs.size());
        System.out.println("  sourceRoots=" + sourceRoots.size());

        Map<String, Path> sourceIndex = buildSourceIndex(sourceRoots);

        // (3) SootUp View 생성
        // - Java 9+에서는 JRT FS input location을 추가해줘야 JDK 타입(java.lang.String 등) 해석이 안정적임 :contentReference[oaicite:4]{index=4}
        String cp = classDirs.stream().map(Path::toString).collect(Collectors.joining(File.pathSeparator));

        List<AnalysisInputLocation> locs = new ArrayList<AnalysisInputLocation>();
        locs.add(new JrtFileSystemAnalysisInputLocation());                 // JDK runtime (Java>=9) :contentReference[oaicite:5]{index=5}
        locs.add(new JavaClassPathAnalysisInputLocation(cp));               // application bytecode :contentReference[oaicite:6]{index=6}

        JavaView view = new JavaView(locs);

        List<MethodInfo> result = new ArrayList<MethodInfo>();
        Map<String, CompilationUnit> cuCache = new HashMap<String, CompilationUnit>();

        // getClasses()는 Stream을 반환(내부 캐시 채움) :contentReference[oaicite:7]{index=7}
        List<JavaSootClass> classes = view.getClasses().collect(Collectors.toList());
        for (JavaSootClass sc : classes) {

            // JRT까지 넣었기 때문에, application만 필터링(표준 라이브러리 제외)
            if (!sc.isApplicationClass()) continue;

            if (!sc.isPublic() || sc.isInterface() || sc.getName().contains("$")) continue;

            String fqn = sc.getName();
            String simple = simpleName(fqn);
            String pkg = packageName(fqn);

            for (SootMethod sm : sc.getMethods()) {

                if (!isEligible(sc, sm)) continue;

                StmtGraph<?> graph;
                try {
                    graph = sm.getBody().getStmtGraph();
                } catch (Throwable t) {
                    continue;
                }

                List<? extends BasicBlock<?>> blocks = new ArrayList<>(graph.getBlocks());
                int nodes = blocks.size();
                int edges = 0;
                for (BasicBlock<?> b : blocks) edges += b.getSuccessors().size();// :contentReference[oaicite:10]{index=10}
                int cc = edges - nodes + 2;

                if (nodes == 0 || edges == 0) continue;
                if (cc <= 0) continue;

                Map<BasicBlock<?>, Integer> b2i = new HashMap<>();
                for (int i = 0; i < blocks.size(); i++) b2i.put(blocks.get(i), i);

                // flow summary (조건 분기 블록만)
                List<String> flow = new ArrayList<String>();
                for (BasicBlock<?> blk : blocks) {
                    List<? extends BasicBlock<?>> succs = blk.getSuccessors();
                    if (succs.size() < 2) continue;

                    Stmt tail = blk.getTail(); // :contentReference[oaicite:11]{index=11}
                    String cond = tail.toString();

                    int from = b2i.get(blk);

                    // JIfStmt: successor 인덱스 규칙이 명확함(0: fallthrough, 1: branching) :contentReference[oaicite:12]{index=12}
                    if (tail instanceof JIfStmt && succs.size() >= 2) {
                        JIfStmt ifs = (JIfStmt) tail;
                        cond = ifs.getCondition().toString(); // :contentReference[oaicite:13]{index=13}

                        int falseBid = b2i.get(succs.get(JIfStmt.FALSE_BRANCH_IDX));
                        int trueBid  = b2i.get(succs.get(JIfStmt.TRUE_BRANCH_IDX));
                        flow.add(String.format("B%d : If(%s) -> B%d | else -> B%d", from, cond, trueBid, falseBid));
                    } else {
                        // switch 등 기타 branching stmt는 그냥 toString() 기반 요약
                        int s0 = b2i.get(succs.get(0));
                        int s1 = b2i.get(succs.get(1));
                        flow.add(String.format("B%d : Branch(%s) -> B%d | B%d", from, cond, s0, s1));
                    }
                }

                // pretty CFG blocks/edges
                Map<String,List<String>> pretty = buildPrettyCFG(blocks, b2i);

                // JavaParser로 소스 찾기
                Path srcPath = findSourcePathByFqn(sourceRoots, fqn);
                if (srcPath == null) srcPath = sourceIndex.get(simple);

                CompilationUnit cu = loadCU(srcPath, cuCache);

                String bodySrc = "(source not found)";
                ClassOrInterfaceDeclaration cdAst = null;

                if (cu != null) {
                    Optional<ClassOrInterfaceDeclaration> cd = cu.getClassByName(simple);
                    if (cd.isPresent()) {
                        cdAst = cd.get();
                        Optional<MethodDeclaration> md = cd.get().getMethods().stream()
                                .filter(m -> matches(m, sm))
                                .findFirst();
                        if (md.isPresent()) {
                            bodySrc = md.get().getBody().map(Object::toString).orElse("(no body)");
                        }
                    }
                }

                // 의존성 분석 (기존 로직 유지)
                Set<String> classDeps = new HashSet<String>();
                Set<List<String>> selfMethodDeps = new HashSet<List<String>>();

                if (cdAst != null) {
                    Optional<MethodDeclaration> optMd = cdAst.getMethods().stream()
                            .filter(m -> matches(m, sm))
                            .findFirst();

                    if (optMd.isPresent()) {
                        MethodDeclaration md = optMd.get();
                        classDeps.addAll(findInternalClassDeps(md, pkg));
                        selfMethodDeps.addAll(findSelfMethodDeps(md));
                    }
                }

                MethodInfo mi = new MethodInfo();

                // depClasses
                for (String clsSimpleOrFqn : classDeps) {
                    String depSimple = clsSimpleOrFqn.contains(".")
                            ? clsSimpleOrFqn.substring(clsSimpleOrFqn.lastIndexOf('.')+1)
                            : clsSimpleOrFqn;

                    String fqnDep = clsSimpleOrFqn.contains(".")
                            ? clsSimpleOrFqn
                            : (pkg.isEmpty() ? depSimple : (pkg + "." + depSimple));

                    Path depSrc = findSourcePathByFqn(sourceRoots, fqnDep);
                    if (depSrc == null) depSrc = sourceIndex.get(depSimple);

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
                    di.name = simple + "#" + mName + "(..." + arity + ")";

                    String bodyTxt = "(source not found)";
                    if (cdAst != null) {
                        bodyTxt = findMethodBodySrc(cdAst, mName, Collections.nCopies(arity, ""));
                    }
                    if ("(source not found)".equals(bodyTxt)) {
                        Path clsPath = sourceIndex.get(simple);
                        CompilationUnit altCu = loadCU(clsPath, cuCache);
                        if (altCu != null) {
                            Optional<ClassOrInterfaceDeclaration> altCd = altCu.getClassByName(simple);
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
                mi.signature   = String.valueOf(sm.getSubSignature());
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

    /* ========= SootUp eligibility ========= */
    private static boolean isEligible(sootup.java.core.JavaSootClass c, sootup.core.model.SootMethod m) {
        if (!c.isPublic() || c.isInterface() || c.getName().contains("$")) return false;
        if (!m.isConcrete()) return false;

        if (!m.isPublic()) return false;

        // 생성자/클래스 초기화 제외
        if ("<init>".equals(m.getName())) return false;
        if ("<clinit>".equals(m.getName())) return false;

        // bridge/synthetic 제외 (버전 차이 대비해서 name()로도 방어 가능)
        for (sootup.core.model.MethodModifier mm : m.getModifiers()) {
            String n = mm.name();
            if ("BRIDGE".equals(n) || "SYNTHETIC".equals(n)) return false;
        }
        return true;
    }

    /* ========= CFG pretty print (SootUp) ========= */
    private static Map<String, List<String>> buildPrettyCFG(
        List<? extends BasicBlock<?>> blocks,
        Map<BasicBlock<?>, Integer> b2i) {

        List<String> outBlocks = new ArrayList<String>();
        List<String> outEdges  = new ArrayList<String>();

        for (BasicBlock<?> b : blocks) {
            int bi = b2i.get(b);

            StringBuilder sb = new StringBuilder();
            sb.append("B").append(bi).append(" {");

            // BasicBlock.getStmts() :contentReference[oaicite:15]{index=15}
            for (Stmt s : b.getStmts()) {
                sb.append("\n  ").append(strip(s.toString()));
            }
            sb.append("\n}");
            outBlocks.add(sb.toString());

            List<? extends BasicBlock<?>> succs = b.getSuccessors();
            if (succs.isEmpty()) {
                outEdges.add("B" + bi + " --> [EXIT]");
            } else {
                for (BasicBlock<?> s : succs) {
                    Integer si = b2i.get(s);
                    outEdges.add("B" + bi + " --> B" + (si == null ? "?" : si));
                }
            }
        }

        Map<String, List<String>> m = new HashMap<String, List<String>>();
        m.put("blocks", outBlocks);
        m.put("edges", outEdges);
        return m;
    }

    /* ========= 경로 탐색 ========= */
    private static List<Path> findClassDirs(Path projectDir) throws IOException {
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
            roots = only;
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

    /* ========= source matching (SootUp) ========= */
    private static boolean matches(MethodDeclaration src, SootMethod sm) {
        if (!src.getNameAsString().equals(sm.getName())) return false;
        if (src.getParameters().size() != sm.getParameterCount()) return false;

        for (int i = 0; i < src.getParameters().size(); i++) {
            String srcTy  = norm(src.getParameter(i).getType().asString());
            String sootTy = norm(String.valueOf(sm.getParameterType(i)));
            if (!srcTy.equalsIgnoreCase(sootTy)) return false;
        }
        return true;
    }

    private static String norm(String t) {
        t = t.trim();
        int g = t.indexOf('<');
        if (g >= 0) t = t.substring(0, g);
        while (t.endsWith("[]") || t.endsWith("...")) {
            t = t.substring(0, t.length() - (t.endsWith("[]") ? 2 : 3));
        }
        int p = t.lastIndexOf('.');
        return p >= 0 ? t.substring(p + 1) : t;
    }

    private static String strip(String s) {
        if (s.startsWith("@this:"))       return "this";
        if (s.startsWith("@parameter"))   return "param" + s.charAt(10);
        int p = s.lastIndexOf('.');
        return p >= 0 ? s.substring(p+1) : s;
    }

    private static String vis(SootMethod m) {
        if (m.isPublic())    return "public";
        if (m.isProtected()) return "protected";
        if (m.isPrivate())   return "private";
        return "package";
    }

    private static String simpleName(String fqn) {
        int p = fqn.lastIndexOf('.');
        return p >= 0 ? fqn.substring(p + 1) : fqn;
    }

    private static String packageName(String fqn) {
        int p = fqn.lastIndexOf('.');
        return p >= 0 ? fqn.substring(0, p) : "";
    }

    private static Set<String> findInternalClassDeps(MethodDeclaration md, String pkg) {
        Set<String> set = new HashSet<String>();
        md.findAll(com.github.javaparser.ast.type.ClassOrInterfaceType.class)
                .stream()
                .map(t -> t.getNameWithScope())
                .filter(n -> n.contains(".") && !pkg.isEmpty() && n.startsWith(pkg))
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