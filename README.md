# 🐾 Animal Guessing Game

A **20 Questions** style animal guessing game built in Java, powered by **Groq AI (LLaMA 3.3)**. Ask yes/no questions and try to guess the secret animal within 20 tries!

---

## 🎮 How to Play

1. The AI secretly picks an animal
2. Ask **yes/no** questions (e.g. *"Does it live in water?"*)
3. Try to guess the animal within 20 questions
4. To make a guess, type: `is it a lion` or just `lion`
5. Type `give up` to reveal the answer and quit

---

## ✨ Features

- 🤖 Powered by Groq AI (LLaMA 3.3 70B)
- 🐘 Supports multi-word animals (e.g. Blue Whale, Snow Leopard)
- ✅ Smart guess detection
- 💬 Full conversation history tracking
- ⚠️ Warning when only 5 questions remain
- 🏳️ Type `give up` to surrender anytime

---

## 🛠️ Setup & Run

### Requirements
- Java 11 or higher
- Groq API Key → [console.groq.com](https://console.groq.com)

### Steps

```bash
# 1. Clone the repository
git clone https://github.com/NANDGOPALSHARMA-29/animal-guessing-game.git
cd animal-guessing-game

# 2. Add your Groq API key in Animal.java
# Line: static String API_KEY = "your api key of groq";

# 3. Compile
javac Animal.java

# 4. Run
java Animal
```

---

## 📸 Demo

```
========================================
     🐾 20 QUESTIONS ANIMAL GAME 🐾
========================================

AI is choosing an animal... Done!

Guess the animal in 20 questions!
To guess: type 'is it a [animal]' or just the animal name
----------------------------------------

[Question 1/20 | 20 left] > Does it live in water?
AI is thinking... YES

[Question 2/20 | 19 left] > Is it a blue whale?
Checking... CORRECT! ✓

========================================
  Correct! The animal was: BLUE WHALE
  You guessed it in 2 questions!
========================================
```

---

## 🔑 Getting Your API Key

1. Go to [console.groq.com](https://console.groq.com)
2. Create a free account
3. Generate an API Key
4. Paste it in `Animal.java`:
   ```java
   static String API_KEY = "your_api_key_here";
   ```

---

## 📁 Project Structure

```
animal-guessing-game/
└── Animal.java    # Main game file
```

---

## 🤝 Contributing

Pull requests are welcome! Feel free to fork and improve the project.

---

## 📄 License

MIT License

---

> Made with ❤️ by [NANDGOPALSHARMA-29](https://github.com/NANDGOPALSHARMA-29)