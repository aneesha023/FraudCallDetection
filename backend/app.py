from flask import Flask, request, jsonify
from flask_cors import CORS
from werkzeug.utils import secure_filename
from pydub import AudioSegment
import os
import numpy as np
import joblib
import traceback
import nltk
import speech_recognition as sr
import warnings
from sklearn.tree import DecisionTreeClassifier

# Ignore ffmpeg warning
warnings.filterwarnings("ignore", message="Couldn't find ffmpeg or avconv")

# Download NLTK data
nltk.download("punkt")

# Set FFmpeg paths
ffmpeg_path = r"D:\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\bin\\ffmpeg.exe"
ffprobe_path = r"D:\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\ffmpeg-2025-03-31-git-35c091f4b7-essentials_build\\bin\\ffprobe.exe"

os.environ["PATH"] += os.pathsep + os.path.dirname(ffmpeg_path)
AudioSegment.converter = ffmpeg_path
AudioSegment.ffprobe = ffprobe_path

app = Flask(__name__)
CORS(app)
app.config['MAX_CONTENT_LENGTH'] = 10 * 1024 * 1024  # 10 MB limit

MODEL_PATH = "fraud_model.pkl"

# Load model if it exists
model = None
if os.path.exists(MODEL_PATH):
    try:
        model = joblib.load(MODEL_PATH)
        print("✅ Model loaded successfully.")
    except Exception as e:
        print(f"❌ Model not loaded: {e}")


@app.route("/")
def home():
    return jsonify({"message": "📡 Fraud Detection API is live!"})

# --- 📲 Caller Metadata-Based Fraud Detection ---
@app.route("/predict", methods=["POST"])
def predict():
    data = request.get_json(force=True)

    call_duration = data.get("call_duration", 0)
    time_of_day = data.get("time_of_day", 0)
    day_of_week = data.get("day_of_week", 0)
    caller_location_risk = data.get("caller_location_risk", 0.0)

    # Replace with your ML model logic
    prediction = "FRAUD" if caller_location_risk > 0.5 else "SAFE"
    spam_probability = round(caller_location_risk * 100)

    # Dummy response
    return jsonify({
        "classification": prediction,
        "spam_probability": spam_probability,
        "phone_number": "+1-202-555-0176",
        "location": "New York, USA",
        "voip": True,
        "risk_level": "High" if caller_location_risk > 0.7 else "Moderate"
    })
import speech_recognition as sr

def speech_to_text(audio_path):
    recognizer = sr.Recognizer()
    with sr.AudioFile(audio_path) as source:
        audio = recognizer.record(source)
    try:
        return recognizer.recognize_google(audio)
    except sr.UnknownValueError:
        return "Could not understand audio"
    except sr.RequestError as e:
        return f"API error: {e}"

UPLOAD_FOLDER = 'uploads'
os.makedirs(UPLOAD_FOLDER, exist_ok=True)  # This ensures the folder exists
# --- 🎙️ Live Speech-to-Text Fraud Detection ---
@app.route("/analyze_speech", methods=["POST"])
def analyze_speech():
    if 'audio' not in request.files and 'file' not in request.files:
        return jsonify({"error": "No audio file"}), 400

    # Accept both keys
    file = request.files.get('audio') or request.files.get('file')
    filename = secure_filename(file.filename)
    ext = os.path.splitext(filename)[1] or ".wav"
    original_path = f"temp_uploaded_audio{ext}"
    wav_path = "converted_audio.wav"

    try:
        print("📥 Received audio:", filename)
        file.save(original_path)
        print("👉 Converting using forced format: wav")
        print("📁 File size:", os.path.getsize(original_path))

        # Force conversion to WAV format
        audio = AudioSegment.from_file(original_path)
        print("✅ Audio Duration:", audio.duration_seconds)
        audio.export(wav_path, format="wav")

        # Speech recognition
        recognizer = sr.Recognizer()
        with sr.AudioFile(wav_path) as source:
            print("🎧 Loading audio for speech recognition...")
            audio_data = recognizer.record(source)

        print("🔍 Sending audio to Google Speech API...")
        text = recognizer.recognize_google(audio_data)
        print("📝 Transcribed Text:", text)

        # Clean up
        os.remove(original_path)
        os.remove(wav_path)

        # Keyword detection
        suspicious_keywords = [
            "urgent", "immediate action required", "act now", "limited time", "only today",
            "time-sensitive", "last warning", "final notice", "respond immediately", "your account will be suspended",
            "congratulations", "you’ve won", "free prize", "cash reward", "claim your reward",
            "get rich quick", "no investment needed", "guaranteed returns", "lottery winner", "easy money",
            "government notice", "irs", "bank alert", "police", "court notice",
            "fbi", "legal action", "account verification", "compliance required", "restricted access",
            "password expired", "security alert", "unusual activity", "suspicious login", "reset your password",
            "two-factor disabled", "data breach", "your account is compromised", "virus detected", "malware warning",
            "this is a confidential matter", "i’m calling from tech support", "we detected a problem with your computer",
            "click the link", "verify your identity", "provide your credentials", "confirm your details",
            "enter your otp", "share your account number", "do not tell anyone","press"
        ]

        label = "Fraud" if any(k in text.lower() for k in suspicious_keywords) else "Legit"

        response = {
            "transcription": text,
            "classification": label
        }

        print("✅ Response:", response)
        return jsonify(response), 200

    except sr.UnknownValueError:
        return jsonify({"transcription": None, "classification": "UNKNOWN", "error": "Could not understand audio"}), 200
    except sr.RequestError:
        return jsonify({"transcription": None, "classification": "UNKNOWN", "error": "Google Speech API failed"}), 200
    except Exception as e:
        print("❌ ERROR:", traceback.format_exc())
        return jsonify({"transcription": None, "classification": "UNKNOWN", "error": str(e)}), 200

# --- 🔁 Retrain Model on Demand ---
@app.route('/retrain_model', methods=['POST'])
def retrain_model():
    global model
    try:
        # Dummy training data — replace with real data if available
        X = np.array([
            [120, 5, 1, 0.9],   # Fraud
            [60, 1, 0, 0.2],    # Legit
            [240, 10, 1, 0.85], # Fraud
            [30, 2, 0, 0.1],    # Legit
            [180, 7, 1, 0.8],   # Fraud
            [45, 1, 0, 0.3]     # Legit
        ])
        y = np.array([1, 0, 1, 0, 1, 0])  # 1 = Fraud, 0 = Legit

        # Train and save model
        model = DecisionTreeClassifier()
        model.fit(X, y)
        joblib.dump(model, MODEL_PATH)
        return jsonify({"message": "✅ Model retrained and saved."})
    except Exception as e:
        return jsonify({"error": str(e)}), 500

if __name__ == '__main__':
    app.run(host="0.0.0.0", port=5000)
