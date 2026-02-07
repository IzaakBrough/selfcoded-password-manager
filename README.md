# JavaFX Password Manager

⚠️ **EDUCATIONAL PROJECT - NOT FOR PRODUCTION USE**

This is a learning project demonstrating JavaFX, encryption, and password management concepts.
DO NOT use this to store real, sensitive passwords. Use established password managers like
Bitwarden, 1Password, or KeePass for actual password storage.

## Features

- **Secure Master Password Authentication** - BCrypt hashed master password with strength 12
- **AES-256-CBC Encryption** - All passwords encrypted using industry-standard encryption
- **Password Generator** - Generate strong passwords with customizable options
- **Password Strength Indicator** - Visual feedback on password strength
- **Search and Filter** - Quickly find your password entries
- **User-Friendly Interface** - Modern JavaFX UI with intuitive controls
- **Auto-Save** - Automatically saves changes to encrypted vault

## Security Features

### Master Password
- Hashed using BCrypt with salt (strength 12)
- Minimum 8 characters required
- Hash stored in `~/.passwordmanager/master.hash`

### Vault Encryption
- AES-256-CBC encryption for all stored passwords
- PBKDF2 key derivation with 65,536 iterations
- HMAC-SHA256 for key generation
- Random IV (Initialization Vector) for each encryption operation
- Encrypted vault stored in `~/.passwordmanager/vault.enc`

### Password Storage
- All entries stored as encrypted JSON
- Passwords never stored in plaintext
- Base64 encoding for encrypted data

## Requirements

- Java 17 or higher
- JavaFX 21
- Gradle (wrapper included)

## Building and Running

### Run the application
```bash
./gradlew run
```

### Build JAR file
```bash
./gradlew jar
```

The JAR file will be created in `build/libs/`

### Run from JAR
```bash
java -jar build/libs/selfcoded-password-manager-1.0-SNAPSHOT.jar
```

## Project Structure

```
src/main/java/com/passwordmanager/
├── App.java                           # Main application entry point
├── controller/
│   ├── LoginController.java           # Login screen controller
│   └── MainController.java            # Main window controller
├── model/
│   └── PasswordEntry.java             # Password entry data model
├── service/
│   ├── EncryptionService.java         # AES-256 encryption/decryption
│   ├── VaultService.java              # Password vault management
│   └── PasswordGeneratorService.java  # Password generation
└── util/
    └── PasswordStrength.java          # Password strength calculation

src/main/resources/
├── fxml/
│   ├── login.fxml                     # Login screen layout
│   └── main.fxml                      # Main window layout
└── css/
    └── style.css                      # Application styling
```

## Usage

### First Time Setup
1. Launch the application
2. Create a new vault by entering a master password (minimum 8 characters)
3. Click "Create New Vault"

### Subsequent Logins
1. Launch the application
2. Enter your master password
3. Click "Login"

### Managing Passwords
- **Add Entry**: Click "Add Entry" button in toolbar
- **Edit Entry**: Click "Edit" button in the Actions column
- **Delete Entry**: Click "Delete" button in the Actions column
- **Copy Password**: Click "Copy" button to copy password to clipboard
- **Search**: Use the search bar to filter entries by website, username, or notes
- **Generate Password**: Click "Generate Password" to create a strong password

### Password Generator
- Adjustable length (8-32 characters)
- Options: uppercase, lowercase, numbers, symbols
- Real-time strength indicator
- Copy to clipboard functionality

## Data Storage

All data is stored in your home directory under `.passwordmanager/`:
- `~/.passwordmanager/master.hash` - BCrypt hash of master password
- `~/.passwordmanager/vault.enc` - Encrypted password vault

## Security Warnings

⚠️ **Important Security Considerations:**

1. This is an **educational project** and has not undergone professional security auditing
2. Use established, professionally-audited password managers for real password storage
3. The master password cannot be recovered if forgotten - all data will be lost
4. Always use a strong, unique master password
5. Keep your system secure with up-to-date antivirus and OS patches
6. Do not share your master password with anyone
7. Regular backups of `~/.passwordmanager/` are recommended

## Technologies Used

- **JavaFX 21** - UI framework
- **Gson** - JSON serialization/deserialization
- **BCrypt** - Password hashing
- **Java Cryptography** - AES-256-CBC encryption, PBKDF2 key derivation
- **Gradle** - Build automation

## License

This project is for educational purposes only. Use at your own risk.

## Contributing

This is a learning project. Feel free to fork and experiment, but remember this is not suitable for production use.
