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
    public static final String MODEL_GPT_5 = "gpt-5";
    public static final String MODEL_GPT_5_MINI = "gpt-5-mini";
    public static final String MODEL_GPT_5_NANO = "gpt-5-nano";

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
    public static final String MODEL_GEMINI_20_FLASH = "gemini-2.0-flash";
    public static final String MODEL_GEMINI_25_FLASH = "gemini-2.5-flash";
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
    public static final String DISPLAY_GPT_5 = "GPT-5";
    public static final String DISPLAY_GPT_5_MINI = "GPT-5 Mini";
    public static final String DISPLAY_GPT_5_NANO = "GPT-5 Nano";
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
    public static final String DISPLAY_GEMINI_20_FLASH = "Gemini 2.0 Flash";
    public static final String DISPLAY_GEMINI_25_FLASH = "Gemini 2.5 Flash";

    // Model Capabilities - Context Tokens
    public static final int CONTEXT_TOKENS_GPT_4O = 128000;
    public static final int CONTEXT_TOKENS_GPT_4O_MINI = 128000;
    public static final int CONTEXT_TOKENS_GPT_4_1 = 128000;
    public static final int CONTEXT_TOKENS_GPT_4_TURBO = 128000;
    public static final int CONTEXT_TOKENS_GPT_4 = 8192;
    public static final int CONTEXT_TOKENS_O1_PREVIEW = 128000;
    public static final int CONTEXT_TOKENS_GPT_35_TURBO = 16385;
    public static final int CONTEXT_TOKENS_GPT_5_NANO = 400000;
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
    public static final int CONTEXT_TOKENS_GEMINI_20_FLASH = 1000000;
    public static final int CONTEXT_TOKENS_GEMINI_25_FLASH = 1000000;

    // Model Capabilities - Output Tokens
    public static final int OUTPUT_TOKENS_GPT_4O = 4096;
    public static final int OUTPUT_TOKENS_GPT_4O_MINI = 16384;
    public static final int OUTPUT_TOKENS_GPT_4_1 = 4096;
    public static final int OUTPUT_TOKENS_GPT_4_TURBO = 4096;
    public static final int OUTPUT_TOKENS_GPT_4 = 4096;
    public static final int OUTPUT_TOKENS_O1_PREVIEW = 32768;
    public static final int OUTPUT_TOKENS_GPT_35_TURBO = 4096;
    public static final int OUTPUT_TOKENS_GPT_5_NANO = 128000;
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
    public static final int OUTPUT_TOKENS_GEMINI_20_FLASH = 8192;
    public static final int OUTPUT_TOKENS_GEMINI_25_FLASH = 8192;

    // Vision Capabilities
    public static final boolean SUPPORTS_VISION_GPT_4O = true;
    public static final boolean SUPPORTS_VISION_GPT_4O_MINI = true;
    public static final boolean SUPPORTS_VISION_GPT_4_1 = true;
    public static final boolean SUPPORTS_VISION_GPT_4_TURBO = true;
    public static final boolean SUPPORTS_VISION_GPT_4 = false;
    public static final boolean SUPPORTS_VISION_O1_PREVIEW = false;
    public static final boolean SUPPORTS_VISION_GPT_35_TURBO = false;
    public static final boolean SUPPORTS_VISION_GPT_5_NANO = true;
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
    public static final boolean SUPPORTS_VISION_GEMINI_20_FLASH = true;
    public static final boolean SUPPORTS_VISION_GEMINI_25_FLASH = true;

    // Cost Estimates (per 1K tokens)
    public static final double COST_GPT_4O = 0.005;
    public static final double COST_GPT_4O_MINI = 0.0002;
    public static final double COST_GPT_4_1 = 0.01;
    public static final double COST_GPT_4_TURBO = 0.01;
    public static final double COST_GPT_4 = 0.03;
    public static final double COST_O1_PREVIEW = 0.015;
    public static final double COST_GPT_35_TURBO = 0.001;
    public static final double COST_GPT_5_NANO = 0.00005;
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
    public static final double COST_GEMINI_20_FLASH = 0.0002;
    public static final double COST_GEMINI_25_FLASH = 0.0003;

    // Token Estimation
    public static final int CHARS_PER_TOKEN_ESTIMATE = 4;

    // Prompts
    public static final String FLASHCARD_JSON_SCHEMA = """
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

            """ + FLASHCARD_JSON_SCHEMA;

    public static final String IMAGE_FLASHCARD_GENERATION_TEMPLATE = """
            Analyze the provided image and generate exactly {count} flashcards based on its content.

            Additional context: {prompt}

            Requirements:
            - Create educational flashcards that extract key concepts from the image
            - Focus on text, code, diagrams, formulas, or any educational content visible
            - Each flashcard should test understanding of different aspects shown
            - If the image contains code, create flashcards about the code patterns, syntax, or logic
            - If the image contains diagrams, create flashcards about relationships and concepts
            - Include code examples where relevant
            - Generate relevant tags based on the content

            """ + FLASHCARD_JSON_SCHEMA;

    public static final String PROMPT_FLASHCARD_GENERATION_TEMPLATE = """
            Generate exactly {count} educational flashcards based on the following request:

            {prompt}

            Topic area: {topic}

            Requirements:
            - Create comprehensive flashcards that cover the requested subject matter
            - Ensure factual accuracy and educational value
            - Include a mix of conceptual questions and practical applications
            - Add code examples where appropriate for programming topics
            - Vary the difficulty levels appropriately
            - Generate relevant tags for categorization

            """ + FLASHCARD_JSON_SCHEMA;

    public static final String SUMMARY_GENERATION_TEMPLATE = """
            Generate a {length} summary in {format} format based on the following content:

            {content}

            Additional requirements: {prompt}

            Guidelines:
            - Focus on the most important concepts and key takeaways
            - Maintain logical flow and coherence
            - Use clear and concise language
            - Preserve technical accuracy for code and technical topics
            - Target approximately {wordCount} words

            Return only the summary text in the requested format, without any additional commentary.
            """;

    // Summary word count targets
    public static final int SUMMARY_SHORT_WORDS = 100;
    public static final int SUMMARY_MEDIUM_WORDS = 250;
    public static final int SUMMARY_LONG_WORDS = 500;
    public static final int SUMMARY_DETAILED_WORDS = 1000;

    // OpenAI Audio Models - Text-to-Speech
    public static final String MODEL_TTS_1 = "tts-1";
    public static final String MODEL_TTS_1_HD = "tts-1-hd";
    public static final String DEFAULT_TTS_MODEL = MODEL_TTS_1;

    // OpenAI Audio Models - Speech-to-Text
    public static final String MODEL_WHISPER_1 = "whisper-1";
    public static final String DEFAULT_STT_MODEL = MODEL_WHISPER_1;

    // TTS Voice Options
    public static final String VOICE_ALLOY = "alloy";
    public static final String VOICE_ECHO = "echo";
    public static final String VOICE_FABLE = "fable";
    public static final String VOICE_ONYX = "onyx";
    public static final String VOICE_NOVA = "nova";
    public static final String VOICE_SHIMMER = "shimmer";
    public static final String DEFAULT_VOICE = VOICE_ALLOY;

    // TTS Voice Descriptions
    public static final String VOICE_DESC_ALLOY = "Neutral and balanced";
    public static final String VOICE_DESC_ECHO = "Male, clear and articulate";
    public static final String VOICE_DESC_FABLE = "British accent, expressive";
    public static final String VOICE_DESC_ONYX = "Deep and authoritative";
    public static final String VOICE_DESC_NOVA = "Female, warm and friendly";
    public static final String VOICE_DESC_SHIMMER = "Female, soft and gentle";

    // Audio Output Types
    public static final String AUDIO_OUTPUT_RECITATION = "RECITATION";
    public static final String AUDIO_OUTPUT_SUMMARY = "SUMMARY";

    // Audio Format Options
    public static final String AUDIO_FORMAT_MP3 = "mp3";
    public static final String AUDIO_FORMAT_OPUS = "opus";
    public static final String AUDIO_FORMAT_AAC = "aac";
    public static final String AUDIO_FORMAT_FLAC = "flac";
    public static final String DEFAULT_AUDIO_FORMAT = AUDIO_FORMAT_MP3;

    // TTS Speed Range (0.25 to 4.0)
    public static final double TTS_SPEED_MIN = 0.25;
    public static final double TTS_SPEED_MAX = 4.0;
    public static final double TTS_SPEED_DEFAULT = 1.0;

    // Audio Processing Limits
    public static final int MAX_AUDIO_TEXT_LENGTH = 4096;
    public static final int MAX_AUDIO_FILE_SIZE_MB = 25;

    // Audio Summarization Template
    public static final String AUDIO_SUMMARY_TEMPLATE = """
            Create a clear, concise audio-friendly summary of the following content.
            The summary should be natural when read aloud and suitable for listening.

            Content:
            {text}

            Requirements:
            - Use simple, clear language appropriate for audio
            - Avoid complex sentence structures
            - Focus on main points and key takeaways
            - Keep it engaging for listeners
            - Target length: {wordCount} words

            Return only the summary text, without any additional commentary.
            """;

    // Transcription Options
    public static final String TRANSCRIPTION_LANGUAGE_AUTO = "auto";
    public static final String TRANSCRIPTION_LANGUAGE_EN = "en";
    public static final String TRANSCRIPTION_LANGUAGE_ES = "es";
    public static final String TRANSCRIPTION_LANGUAGE_FR = "fr";
    public static final String TRANSCRIPTION_LANGUAGE_DE = "de";

    // STT Response Format Options
    public static final String STT_RESPONSE_FORMAT_JSON = "json";
    public static final String STT_RESPONSE_FORMAT_TEXT = "text";
    public static final String STT_RESPONSE_FORMAT_SRT = "srt";
    public static final String STT_RESPONSE_FORMAT_VTT = "vtt";
    public static final String DEFAULT_STT_RESPONSE_FORMAT = STT_RESPONSE_FORMAT_JSON;


    private AIConstants() {
        // Private constructor to prevent instantiation
    }
}