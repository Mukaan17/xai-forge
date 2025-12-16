# @Date:   2025-09-04 16:26:13
# @Last Modified by:   Mukhil Sundararaj
# @Last Modified time: 2025-10-24 18:35:11
#!/bin/bash

# XAI-Forge GitHub Preparation Script
# This script prepares the project for GitHub upload

echo "🚀 Preparing XAI-Forge for GitHub..."

# Check if we're in the right directory
if [ ! -f "README.md" ] || [ ! -d "backend" ] || [ ! -d "frontend" ]; then
    echo "❌ Error: Please run this script from the project root directory"
    exit 1
fi

# Create upload directories if they don't exist
echo "📁 Creating upload directories..."
mkdir -p uploads/datasets
mkdir -p uploads/models

# Set proper permissions
chmod 755 uploads/
chmod 755 uploads/datasets/
chmod 755 uploads/models/

# Make scripts executable
echo "🔧 Setting script permissions..."
chmod +x start.sh
chmod +x prepare-github.sh

# Check for sensitive files that shouldn't be committed
echo "🔍 Checking for sensitive files..."
if [ -f "backend/src/main/resources/application.properties" ]; then
    echo "⚠️  Warning: application.properties contains sensitive data"
    echo "   Make sure to update it with your actual database credentials"
    echo "   Use config.template.properties as a reference"
fi

# Verify essential files exist
echo "✅ Verifying essential files..."
required_files=(
    "README.md"
    "LICENSE"
    "CONTRIBUTING.md"
    "CHANGELOG.md"
    ".gitignore"
    "pom.xml"
    "backend/pom.xml"
    "frontend/package.json"
    "docs/README.md"
    "docs/SETUP-GUIDE.md"
    "docs/USER-GUIDE.md"
    "docs/API-GUIDE.md"
    "docs/ARCHITECTURE.md"
    ".github/workflows/ci.yml"
    ".github/ISSUE_TEMPLATE/bug_report.md"
    ".github/ISSUE_TEMPLATE/feature_request.md"
    ".github/pull_request_template.md"
)

missing_files=()
for file in "${required_files[@]}"; do
    if [ ! -f "$file" ]; then
        missing_files+=("$file")
    fi
done

if [ ${#missing_files[@]} -eq 0 ]; then
    echo "✅ All essential files are present"
else
    echo "❌ Missing files:"
    for file in "${missing_files[@]}"; do
        echo "   - $file"
    done
    exit 1
fi

# Check project structure
echo "🏗️  Verifying project structure..."
required_dirs=(
    "backend/src/main/java/com/example/xaiapp"
    "frontend/src"
    "docs"
    ".github/workflows"
    ".github/ISSUE_TEMPLATE"
    "uploads"
)

missing_dirs=()
for dir in "${required_dirs[@]}"; do
    if [ ! -d "$dir" ]; then
        missing_dirs+=("$dir")
    fi
done

if [ ${#missing_dirs[@]} -eq 0 ]; then
    echo "✅ Project structure is correct"
else
    echo "❌ Missing directories:"
    for dir in "${missing_dirs[@]}"; do
        echo "   - $dir"
    done
    exit 1
fi

# Display next steps
echo ""
echo "🎉 XAI-Forge is ready for GitHub!"
echo ""
echo "📋 Next steps:"
echo "1. Initialize git repository:"
echo "   git init"
echo ""
echo "2. Add all files:"
echo "   git add ."
echo ""
echo "3. Make initial commit:"
echo "   git commit -m 'Initial commit: XAI-Forge full-stack application'"
echo ""
echo "4. Create repository on GitHub and add remote:"
echo "   git remote add origin https://github.com/your-username/xai-forge.git"
echo ""
echo "5. Push to GitHub:"
echo "   git branch -M main"
echo "   git push -u origin main"
echo ""
echo "⚠️  Important reminders:"
echo "- Update database credentials in application.properties"
echo "- Replace 'your-username' in README.md with your actual GitHub username"
echo "- Review and update the PROJECT_STATUS.md file"
echo "- Test the application locally before pushing"
echo ""
echo "🔗 Useful links:"
echo "- Documentation: docs/README.md"
echo "- Setup Guide: docs/SETUP-GUIDE.md"
echo "- Project Status: PROJECT_STATUS.md"
echo "- Contributing: CONTRIBUTING.md"
echo ""
echo "Happy coding! 🚀"
