# 🐣 Tamagotchi - L2H1
Paris Cité student project - group L2H1

Welcome to the Tamagotchi project repository!

## ⚙️ For Android Developers (Android Studio)

1. **Clone the Repository**: 
   - Clone the repository using `git clone https://github.com/pacotine/tamagotchi.git`.
   - Access the source folder for the version of your choice in `src/`.

2. **Configure the Project**:
   - Add a `local.properties` file at the root of the project if not already present. Declare the variable that points to the path to your SDK (`sdk.dir=path/to/your/SDK`).
     
     <ins>Warning</ins>: Do not save this file in a Version Control System (VCS) as it contains information specific to your local configuration.
   - Retrieve and add, if not already present, a `google-services.json` file in the `app/` folder.
     - Step 1: Connect to the application's Firebase database.
     - Step 2: Click on the menu gear, then select Project settings.
     - Step 3: In the Your applications tab, select the package `com.l2h1.tamagotchi`.
     - Step 4: Click on the *google-services.json* download button to retrieve the file.

3. **Sync with Gradle and Build the Project**.

## 🙋 For Users

1. **Visit Our Website**: 
   - Go to [pacotine.github.io/tamagotchi/](https://pacotine.github.io/tamagotchi/).
  
2. **Download the APK**:
   - Click on the APK download button for the version of your choice.
  
3. **Override Browser Warnings**:
   - Override your browser's warning messages about the potential risk of downloading APKs from unknown sources (however, always remain vigilant for other cases).
  
4. **Install the Application**:
   - If your browser suggests, install the application via the downloaded APK. Otherwise, search for the downloaded APK in your file explorer, then proceed with the installation.
  
5. **Launch the Application**: 
   - Once the application is installed, you can launch it.
   
## 📂 Documentation

The documentation is present in the source code in the 3 versions of the `src/` folder.

You can also find the Javadoc generated in the `doc/` folder or directly on the site by clicking on *Javadoc*.
