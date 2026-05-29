# Java 26 Setup Guide for Windows

## STATUS: ✅ Project Updated to Java 26

Your `pom.xml` has been updated to use **Java 26** to match your OpenJDK 26 installation.

---

## Step 1: Find Your OpenJDK 26 Installation

Since you mentioned you have OpenJDK 26 installed, you need to locate it. Common installation paths:

```
C:\Program Files\openjdk-26
C:\Program Files\Java\openjdk-26
C:\Program Files (x86)\Java\openjdk-26
D:\Java\openjdk-26
%APPDATA%\JetBrains\IntelliJ_*/jbr (IntelliJ's bundled JDK)
```

**To find it:**

1. Open PowerShell as Administrator
2. Run these commands:

```powershell
# Search in Program Files
Get-ChildItem "C:\Program Files" -Recurse -Filter "java.exe" 2>/dev/null | Select-Object Directory

# Search in Program Files (x86)
Get-ChildItem "C:\Program Files (x86)" -Recurse -Filter "java.exe" 2>/dev/null | Select-Object Directory

# Search in AppData (IntelliJ bundled JDK)
Get-ChildItem "$env:APPDATA\JetBrains" -Recurse -Filter "java.exe" 2>/dev/null | Select-Object Directory
```

The result should look something like:
```
C:\Program Files\openjdk-26\bin\java.exe
```

Note the parent directory path (without `\bin\java.exe`).

---

## Step 2: Set JAVA_HOME Environment Variable

Once you've found your OpenJDK 26 installation:

### Option A: Set Permanently (Recommended)

1. Open **Environment Variables**:
   - Press `Win + R`
   - Type: `sysdm.cpl`
   - Click **Environment Variables** button

2. Click **New** under "System variables"

3. Variable name: `JAVA_HOME`
   
4. Variable value: `C:\Program Files\openjdk-26` (replace with your actual path)

5. Click **OK** and **OK**

6. **Restart PowerShell or Command Prompt** for changes to take effect

### Option B: Set Temporarily (Current Session Only)

Open PowerShell and run:

```powershell
$env:JAVA_HOME = "C:\Program Files\openjdk-26"
# verify it's set
$env:JAVA_HOME
```

---

## Step 3: Verify Installation

After setting `JAVA_HOME`, verify it's working:

```powershell
# Check Java version
java -version

# Check JAVA_HOME is set
$env:JAVA_HOME

# Check Maven finds Java
cd "C:\Users\arunr\IdeaProjects\mySpringProject\mySpringProject"
.\mvnw.cmd --version
```

Expected output for `java -version`:
```
openjdk version "26" 2024-03-19
OpenJDK Runtime Environment (build 26+43-2973)
OpenJDK 64-Bit Server VM (build 26+43-2973, mixed mode, sharing)
```

---

## Step 4: Configure IntelliJ IDEA

If IntelliJ still shows Java errors:

1. Open IntelliJ IDEA
2. Go to **File → Project Structure**
3. Select **Project** from left panel
4. Under "SDK" dropdown, click **Edit** or **Add**
5. Click **+** and select **Add JDK**
6. Navigate to your OpenJDK 26 installation folder (e.g., `C:\Program Files\openjdk-26`)
7. Click **Open** → IntelliJ will detect it
8. Name it "openjdk-26"
9. Click **OK** → **OK**
10. Set it as your Project SDK
11. Click **OK** to close Project Structure

---

## Step 5: Build Your Project

Once JAVA_HOME is set and IntelliJ is configured:

```powershell
cd "C:\Users\arunr\IdeaProjects\mySpringProject\mySpringProject"

# Clean build
.\mvnw.cmd clean package

# Or run directly
.\mvnw.cmd spring-boot:run
```

---

## Troubleshooting

### Issue: "JAVA_HOME environment variable is not defined correctly"

**Solution:**
1. Check `JAVA_HOME` is set correctly:
   ```powershell
   $env:JAVA_HOME
   ```

2. Verify the path points to JDK (not JRE):
   ```powershell
   Test-Path "$env:JAVA_HOME\bin\java.exe"
   ```
   Should return `True`

3. Restart PowerShell after changing environment variables

### Issue: Maven still not finding Java after setting JAVA_HOME

**Solution - Use Maven wrapper directly:**
```powershell
cd "C:\Users\arunr\IdeaProjects\mySpringProject\mySpringProject"

# This should work even without JAVA_HOME if you have Java installed
.\mvnw.cmd -v
.\mvnw.cmd clean package
```

### Issue: IntelliJ shows "Project SDK is not defined"

**Solution:**
1. Go to **File → Project Structure**
2. Click **Project** 
3. In SDK dropdown, if empty, click **Edit** → **+** → **Add JDK**
4. Browse to your OpenJDK 26 folder
5. Click OK

---

## Alternative: Use IntelliJ's Bundled JDK

If you don't want to manually configure Java, IntelliJ comes with a bundled JDK:

1. Open IntelliJ
2. Go to **File → Project Structure → Project**
3. In SDK dropdown → **Edit** → **+** → **Download JDK**
4. Select version **26** from the dropdown
5. Choose **OpenJDK** vendor
6. Click **Download** → IntelliJ will automatically configure it

---

## Final Steps: Run Your GitHub API

Once Java 26 is set up:

```powershell
cd "C:\Users\arunr\IdeaProjects\mySpringProject\mySpringProject"

# Build the project
.\mvnw.cmd clean package

# Run the Spring Boot application
.\mvnw.cmd spring-boot:run

# Application will be available at http://localhost:8080
```

Test the API:
```powershell
curl -X GET "http://localhost:8080/api/github/users/octocat"
```

---

## Project Configuration Summary

- **Java Version:** Java 26
- **Spring Boot:** 3.5.7
- **Build Tool:** Maven
- **GitHub API Endpoint:** GET `/api/github/users/{username}`
- **Port:** 8080

✅ Your GitHub API is ready to run with Java 26!

---

## Quick Reference

| Task | Command |
|------|---------|
| Check Java version | `java -version` |
| Check JAVA_HOME | `$env:JAVA_HOME` |
| Build project | `.\mvnw.cmd clean package` |
| Run Spring Boot | `.\mvnw.cmd spring-boot:run` |
| Run tests | `.\mvnw.cmd test` |

---

## Need Help?

1. Run `java -version` to verify Java is installed
2. Run `$env:JAVA_HOME` to check environment variable
3. Run `.\mvnw.cmd --version` to verify Maven can find Java
4. Check IntelliJ **File → Settings → Build, Execution, Deployment → Build Tools → Maven** for JDK configuration

If you still have issues, provide the output of:
```powershell
java -version
$env:JAVA_HOME
.\mvnw.cmd --version
```

