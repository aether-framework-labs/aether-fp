## Contributing to Aether FP

Thank you for your interest in contributing to Aether FP! 🎉

We welcome all contributions, whether it's fixing bugs, adding features, improving documentation, or helping with discussions.

---

## 🚀 How to Contribute

### 1️⃣ Fork the Repository
1. Click the **Fork** button at the top of the repository.
2. Clone your fork:
```sh
git clone https://github.com/aether-framework-labs/aether-fp.git
cd aether-fp
```

### 2️⃣ Create a Branch
```sh
git checkout -b feature/new-awesome-feature
```
Use a meaningful branch name (e.g., `fix/lazy-thread-safety` or `feature/option-type`).

### 3️⃣ Implement Your Changes
- Follow the coding style of the project (see below).
- Write tests if applicable (`src/test/java`).
- Ensure the build is successful (`mvn clean install`).

### 4️⃣ Commit and Push
```sh
git add .
git commit -m "Added new awesome feature"
git push origin feature/new-awesome-feature
```

### 5️⃣ Open a Pull Request (PR)
1. Go to the **Pull Requests** tab in the repository.
2. Click **New Pull Request**.
3. Select your branch and describe your changes.
4. Wait for a review and feedback!

---

## 📐 Coding Standards

- **Java 21** — Use modern language features (sealed classes, pattern matching, records where appropriate)
- **`@NotNull` / `@Nullable`** from JetBrains Annotations on all signatures
- **`final`** on method parameters
- **Comprehensive Javadoc** on all classes and methods with `@since` tags
- **Checkstyle** enforced via Google Java Style base (4-space indent)
- **No external dependencies** beyond JetBrains Annotations
- **Strict null prohibition** — all factory methods and function parameters reject `null`

---

## 📜 Contribution Guidelines

- ✅ Keep PRs small and focused.
- ✅ Include clear commit messages.
- ✅ Make sure tests pass before submitting.
- ✅ Discuss major changes in an issue before implementation.
- ✅ Be respectful in discussions and code reviews.

---

## 📢 Need Help?
If you have any questions, feel free to open an **Issue** or join our **Discussions** section.

🚀 **Happy Coding!**
