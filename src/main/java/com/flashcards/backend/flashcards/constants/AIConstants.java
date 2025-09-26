package com.flashcards.backend.flashcards.constants;

public class AIConstants {

    // OpenAI Model Names
    public static final String MODEL_GPT_4 = "gpt-4";
    public static final String MODEL_GPT_4_1 = "gpt-4.1";
    public static final String MODEL_GPT_4_TURBO = "gpt-4-turbo";
    public static final String MODEL_GPT_4_TURBO_PREVIEW = "gpt-4-turbo-preview";
    public static final String MODEL_GPT_4O = "gpt-4o";
    public static final String MODEL_GPT_4O_MINI = "gpt-4o-mini";
    public static final String MODEL_O1_PREVIEW = "o1-preview";
    public static final String MODEL_GPT_35_TURBO = "gpt-3.5-turbo";

    // Anthropic Claude Model Names
    public static final String MODEL_CLAUDE_SONNET_4_20250514 = "claude-sonnet-4-20250514";
    public static final String MODEL_CLAUDE_OPUS_4_1_20250805 = "claude-opus-4-1-20250805";
    public static final String MODEL_CLAUDE_35_SONNET_20241022 = "claude-3-5-sonnet-20241022";
    public static final String MODEL_CLAUDE_35_HAIKU_20241022 = "claude-3-5-haiku-20241022";
    public static final String MODEL_CLAUDE_3_OPUS = "claude-3-opus";
    public static final String MODEL_CLAUDE_3_OPUS_20240229 = "claude-3-opus-20240229";
    public static final String MODEL_CLAUDE_3_SONNET = "claude-3-sonnet";
    public static final String MODEL_CLAUDE_3_SONNET_20240229 = "claude-3-sonnet-20240229";
    public static final String MODEL_CLAUDE_3_HAIKU = "claude-3-haiku";
    public static final String MODEL_CLAUDE_3_HAIKU_20240307 = "claude-3-haiku-20240307";

    // Google Gemini Model Names
    public static final String MODEL_GEMINI_PRO = "gemini-pro";
    public static final String MODEL_GEMINI_15_PRO = "gemini-1.5-pro";
    public static final String MODEL_GEMINI_15_FLASH = "gemini-1.5-flash";
    public static final String MODEL_GEMINI_ULTRA = "gemini-ultra";

    // Default Models
    public static final String DEFAULT_OPENAI_MODEL = MODEL_GPT_4O_MINI;
    public static final String DEFAULT_ANTHROPIC_MODEL = MODEL_CLAUDE_3_SONNET_20240229;
    public static final String DEFAULT_GEMINI_MODEL = MODEL_GEMINI_PRO;

    // Model Provider Hints
    public static final String PROVIDER_HINT_GPT = "gpt";
    public static final String PROVIDER_HINT_CLAUDE = "claude";
    public static final String PROVIDER_HINT_GEMINI = "gemini";

    // AI Configuration Keys
    public static final String CONFIG_MAX_TOKENS = "max-tokens";
    public static final String CONFIG_TEMPERATURE = "temperature";
    public static final String CONFIG_TOP_P = "top-p";
    public static final String CONFIG_FREQUENCY_PENALTY = "frequency-penalty";
    public static final String CONFIG_PRESENCE_PENALTY = "presence-penalty";

    // AI Configuration Default Values
    public static final double DEFAULT_TEMPERATURE = 0.7;

    // AI Rate Limiting
    public static final int MAX_FLASHCARDS_PER_REQUEST = 20;
    public static final int MAX_TEXT_LENGTH = 10000;
    public static final int RATE_LIMIT_PER_MINUTE = 10;
    public static final int CACHE_TTL_SECONDS = 3600;

    // AI Response Parsing
    public static final String JSON_FIELD_QUESTION = "question";
    public static final String JSON_FIELD_ANSWER = "answer";
    public static final String JSON_FIELD_TAGS = "tags";
    public static final String JSON_FIELD_DIFFICULTY = "difficulty";
    public static final String JSON_FIELD_EXPLANATION = "explanation";
    public static final String JSON_FIELD_CODE_BLOCK = "codeBlock";
    public static final String JSON_FIELD_LANGUAGE = "language";
    public static final String JSON_FIELD_FRONT = "front";
    public static final String JSON_FIELD_BACK = "back";
    public static final String JSON_FIELD_HINT = "hint";
    public static final String JSON_FIELD_TEXT = "text";
    public static final String JSON_FIELD_TYPE = "type";
    public static final String JSON_FIELD_CODE_BLOCKS = "codeBlocks";
    public static final String JSON_FIELD_CODE = "code";
    public static final String JSON_FIELD_FILE_NAME = "fileName";
    public static final String JSON_FIELD_HIGHLIGHTED = "highlighted";

    // Vertex AI Specific
    public static final String DEFAULT_VERTEX_LOCATION = "us-central1";
    public static final String VERTEX_TRANSPORT_NAME = "grpc";

    // Model Display Names
    public static final String DISPLAY_GPT_4O = "GPT-4 Omni";
    public static final String DISPLAY_GPT_4O_MINI = "GPT-4 Omni Mini";
    public static final String DISPLAY_GPT_4_1 = "GPT-4.1";
    public static final String DISPLAY_GPT_4_TURBO = "GPT-4 Turbo";
    public static final String DISPLAY_GPT_4 = "GPT-4";
    public static final String DISPLAY_O1_PREVIEW = "O1 Preview";
    public static final String DISPLAY_GPT_35_TURBO = "GPT-3.5 Turbo";
    public static final String DISPLAY_CLAUDE_SONNET_4 = "Claude Sonnet 4";
    public static final String DISPLAY_CLAUDE_OPUS_4_1 = "Claude Opus 4.1";
    public static final String DISPLAY_CLAUDE_35_SONNET = "Claude 3.5 Sonnet";
    public static final String DISPLAY_CLAUDE_35_HAIKU = "Claude 3.5 Haiku";
    public static final String DISPLAY_CLAUDE_3_OPUS = "Claude 3 Opus";
    public static final String DISPLAY_CLAUDE_3_SONNET = "Claude 3 Sonnet";
    public static final String DISPLAY_CLAUDE_3_HAIKU = "Claude 3 Haiku";
    public static final String DISPLAY_GEMINI_PRO = "Gemini Pro";
    public static final String DISPLAY_GEMINI_PRO_VISION = "Gemini Pro Vision";
    public static final String DISPLAY_GEMINI_15_PRO = "Gemini 1.5 Pro";
    public static final String DISPLAY_GEMINI_15_FLASH = "Gemini 1.5 Flash";

    // Model Capabilities - Context Tokens
    public static final int CONTEXT_TOKENS_GPT_4O = 128000;
    public static final int CONTEXT_TOKENS_GPT_4O_MINI = 128000;
    public static final int CONTEXT_TOKENS_GPT_4_1 = 128000;
    public static final int CONTEXT_TOKENS_GPT_4_TURBO = 128000;
    public static final int CONTEXT_TOKENS_GPT_4 = 8192;
    public static final int CONTEXT_TOKENS_O1_PREVIEW = 128000;
    public static final int CONTEXT_TOKENS_GPT_35_TURBO = 16385;
    public static final int CONTEXT_TOKENS_CLAUDE_SONNET_4 = 200000;
    public static final int CONTEXT_TOKENS_CLAUDE_OPUS_4_1 = 200000;
    public static final int CONTEXT_TOKENS_CLAUDE_35_SONNET = 200000;
    public static final int CONTEXT_TOKENS_CLAUDE_35_HAIKU = 200000;
    public static final int CONTEXT_TOKENS_CLAUDE_3_OPUS = 200000;
    public static final int CONTEXT_TOKENS_CLAUDE_3_SONNET = 200000;
    public static final int CONTEXT_TOKENS_CLAUDE_3_HAIKU = 200000;
    public static final int CONTEXT_TOKENS_GEMINI_PRO = 30720;
    public static final int CONTEXT_TOKENS_GEMINI_PRO_VISION = 12288;
    public static final int CONTEXT_TOKENS_GEMINI_15_PRO = 1000000;
    public static final int CONTEXT_TOKENS_GEMINI_15_FLASH = 1000000;

    // Model Capabilities - Output Tokens
    public static final int OUTPUT_TOKENS_GPT_4O = 4096;
    public static final int OUTPUT_TOKENS_GPT_4O_MINI = 16384;
    public static final int OUTPUT_TOKENS_GPT_4_1 = 4096;
    public static final int OUTPUT_TOKENS_GPT_4_TURBO = 4096;
    public static final int OUTPUT_TOKENS_GPT_4 = 4096;
    public static final int OUTPUT_TOKENS_O1_PREVIEW = 32768;
    public static final int OUTPUT_TOKENS_GPT_35_TURBO = 4096;
    public static final int OUTPUT_TOKENS_CLAUDE_SONNET_4 = 8192;
    public static final int OUTPUT_TOKENS_CLAUDE_OPUS_4_1 = 8192;
    public static final int OUTPUT_TOKENS_CLAUDE_35_SONNET = 8192;
    public static final int OUTPUT_TOKENS_CLAUDE_35_HAIKU = 8192;
    public static final int OUTPUT_TOKENS_CLAUDE_3_OPUS = 4096;
    public static final int OUTPUT_TOKENS_CLAUDE_3_SONNET = 4096;
    public static final int OUTPUT_TOKENS_CLAUDE_3_HAIKU = 4096;
    public static final int OUTPUT_TOKENS_GEMINI_PRO = 2048;
    public static final int OUTPUT_TOKENS_GEMINI_PRO_VISION = 4096;
    public static final int OUTPUT_TOKENS_GEMINI_15_PRO = 8192;
    public static final int OUTPUT_TOKENS_GEMINI_15_FLASH = 8192;

    // Vision Capabilities
    public static final boolean SUPPORTS_VISION_GPT_4O = true;
    public static final boolean SUPPORTS_VISION_GPT_4O_MINI = true;
    public static final boolean SUPPORTS_VISION_GPT_4_1 = true;
    public static final boolean SUPPORTS_VISION_GPT_4_TURBO = true;
    public static final boolean SUPPORTS_VISION_GPT_4 = false;
    public static final boolean SUPPORTS_VISION_O1_PREVIEW = false;
    public static final boolean SUPPORTS_VISION_GPT_35_TURBO = false;
    public static final boolean SUPPORTS_VISION_CLAUDE_SONNET_4 = true;
    public static final boolean SUPPORTS_VISION_CLAUDE_OPUS_4_1 = true;
    public static final boolean SUPPORTS_VISION_CLAUDE_35_SONNET = true;
    public static final boolean SUPPORTS_VISION_CLAUDE_35_HAIKU = true;
    public static final boolean SUPPORTS_VISION_CLAUDE_3_OPUS = true;
    public static final boolean SUPPORTS_VISION_CLAUDE_3_SONNET = true;
    public static final boolean SUPPORTS_VISION_CLAUDE_3_HAIKU = true;
    public static final boolean SUPPORTS_VISION_GEMINI_PRO = true;
    public static final boolean SUPPORTS_VISION_GEMINI_PRO_VISION = true;
    public static final boolean SUPPORTS_VISION_GEMINI_15_PRO = true;
    public static final boolean SUPPORTS_VISION_GEMINI_15_FLASH = true;

    // Cost Estimates (per 1K tokens)
    public static final double COST_GPT_4O = 0.005;
    public static final double COST_GPT_4O_MINI = 0.0002;
    public static final double COST_GPT_4_1 = 0.01;
    public static final double COST_GPT_4_TURBO = 0.01;
    public static final double COST_GPT_4 = 0.03;
    public static final double COST_O1_PREVIEW = 0.015;
    public static final double COST_GPT_35_TURBO = 0.001;
    public static final double COST_CLAUDE_SONNET_4 = 0.003;
    public static final double COST_CLAUDE_OPUS_4_1 = 0.015;
    public static final double COST_CLAUDE_35_SONNET = 0.003;
    public static final double COST_CLAUDE_35_HAIKU = 0.001;
    public static final double COST_CLAUDE_3_OPUS = 0.015;
    public static final double COST_CLAUDE_3_SONNET = 0.003;
    public static final double COST_CLAUDE_3_HAIKU = 0.0005;
    public static final double COST_GEMINI_PRO = 0.001;
    public static final double COST_GEMINI_PRO_VISION = 0.001;
    public static final double COST_GEMINI_15_PRO = 0.0035;
    public static final double COST_GEMINI_15_FLASH = 0.0002;

    // Flashcard Recommendations
    public static final boolean RECOMMENDED_GPT_4O = false;
    public static final boolean RECOMMENDED_GPT_4O_MINI = true;
    public static final boolean RECOMMENDED_GPT_4_1 = false;
    public static final boolean RECOMMENDED_GPT_4_TURBO = false;
    public static final boolean RECOMMENDED_GPT_4 = false;
    public static final boolean RECOMMENDED_O1_PREVIEW = false;
    public static final boolean RECOMMENDED_GPT_35_TURBO = false;
    public static final boolean RECOMMENDED_CLAUDE_SONNET_4 = true;
    public static final boolean RECOMMENDED_CLAUDE_OPUS_4_1 = false;
    public static final boolean RECOMMENDED_CLAUDE_35_SONNET = true;
    public static final boolean RECOMMENDED_CLAUDE_35_HAIKU = true;
    public static final boolean RECOMMENDED_CLAUDE_3_OPUS = false;
    public static final boolean RECOMMENDED_CLAUDE_3_SONNET = false;
    public static final boolean RECOMMENDED_CLAUDE_3_HAIKU = true;
    public static final boolean RECOMMENDED_GEMINI_PRO = false;
    public static final boolean RECOMMENDED_GEMINI_PRO_VISION = false;
    public static final boolean RECOMMENDED_GEMINI_15_PRO = false;
    public static final boolean RECOMMENDED_GEMINI_15_FLASH = true;

    // Token Estimation
    public static final int CHARS_PER_TOKEN_ESTIMATE = 4;

    // Prompts
    public static final String FLASHCARD_GENERATION_TEMPLATE = """
            Generate exactly {count} flashcards from the following text content:

            {text}

            Requirements:
            - Create educational flashcards that focus on key concepts
            - Each flashcard should have distinct front and back content
            - Front should be a question or prompt
            - Back should be a clear, concise answer
            - Include code examples where relevant
            - Vary difficulty levels appropriately
            - Generate relevant tags for categorization
            - IMPORTANT: You must generate exactly {count} complete flashcards. DO NOT PRODUCE MORE FLASHCARDS THEN YOU ARE ASKED FOR.

            Return ONLY a valid JSON array with this exact structure (no additional text, no markdown, no explanations):
            [
              {{
                "front": {{
                  "text": "Question or prompt text",
                  "codeBlocks": [
                    {{
                      "language": "java",
                      "code": "example code",
                      "fileName": "optional filename",
                      "highlighted": false
                    }}
                  ],
                  "type": "TEXT_ONLY"
                }},
                "back": {{
                  "text": "Answer or explanation text",
                  "codeBlocks": [],
                  "type": "TEXT_ONLY"
                }},
                "hint": "Optional helpful hint",
                "tags": ["tag1", "tag2"],
                "difficulty": "MEDIUM"
              }}
            ]

            Valid difficulty levels: EASY, MEDIUM, HARD, NOT_SET
            Valid content types: TEXT_ONLY, CODE_ONLY, MIXED

            CRITICAL: Ensure the JSON is complete and properly closed with all required braces and brackets.
            """;


    private AIConstants() {
        // Private constructor to prevent instantiation
    }
}