# config.py
class Config:
    __API_KEY = "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"  # 🔒 프라이빗 변수 (외부에서 직접 접근 불가)
    __PROMPT_DIR = "./prompt"  # 🔒 프라이빗 변수 (외부에서 직접 접근 불가)
    __SYSTEM_PROMPT = "system.txt"  # 🔒 프라이빗 변수 (외부에서 직접 접근 불가)
    __USER_PROMPT = "user.txt"  # 🔒 프라이빗 변수 (외부에서 직접 접근 불가)
    __MAX_TOKENS = 10000  # 🔒 프라이빗 변수 (외부에서 직접 접근 불가)

    MODEL_MAP = {
    "1": "gpt-4o",
    "2": "gpt-4o-mini",
    "3": "o1",
    "4": "o1-mini",
    "5": "o3-mini",
    "6": "o4-mini",
    "7": "gpt-4.1",
    "8": "gpt-5"
    }

    @classmethod
    def get_api_key(cls):
        """API 키 반환 (Getter)"""
        return cls.__API_KEY

    @classmethod
    def set_api_key(cls, new_key): 
        """API 키 변경 (Setter)"""
        cls.__API_KEY = new_key

    @classmethod
    def get_prompt_dir(cls):
        """프롬프트 디렉토리 반환 (Getter)"""
        return cls.__PROMPT_DIR
    
    @classmethod
    def set_prompt_dir(cls, new_dir):
        """프롬프트 디렉토리 변경 (Setter)"""
        cls.__PROMPT_DIR = new_dir

    @classmethod
    def get_system_prompt(cls):
        """시스템 프롬프트 파일명 반환 (Getter)"""
        return cls.__SYSTEM_PROMPT
    
    @classmethod
    def set_system_prompt(cls, new_prompt):
        """시스템 프롬프트 파일명 변경 (Setter)"""
        cls.__SYSTEM_PROMPT = new_prompt

    @classmethod
    def get_user_prompt(cls):
        """사용자 프롬프트 파일명 반환 (Getter)"""
        return cls.__USER_PROMPT
    
    @classmethod
    def set_user_prompt(cls, new_prompt):
        """사용자 프롬프트 파일명 변경 (Setter)"""
        cls.__USER_PROMPT = new_prompt

    @classmethod
    def get_max_tokens(cls):
        """최대 토큰 수 반환 (Getter)"""
        return cls.__MAX_TOKENS

    @classmethod
    def set_max_tokens(cls, new_max_tokens):
        """최대 토큰 수 변경 (Setter)"""
        cls.__MAX_TOKENS = new_max_tokens