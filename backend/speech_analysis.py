import speech_recognition as sr
import nltk
from transformers import pipeline

nltk.download('punkt')

# Load NLP models
keyword_detector = ["lottery", "win", "OTP", "bank", "urgent", "free", "prize","apple"]
sentiment_analyzer = pipeline("sentiment-analysis")

def transcribe_audio(audio_file):
    recognizer = sr.Recognizer()
    with sr.AudioFile(audio_file) as source:
        audio = recognizer.record(source)

    try:
        text = recognizer.recognize_google(audio)
        return text.lower()
    except sr.UnknownValueError:
        return "Error: Could not understand audio"
    except sr.RequestError:
        return "Error: API unavailable"

def analyze_text(transcribed_text):
    scam_detected = any(word in transcribed_text for word in keyword_detector)
    sentiment = sentiment_analyzer(transcribed_text)[0]

    return {
        "transcript": transcribed_text,
        "scam_detected": scam_detected,
        "sentiment": sentiment
    }
