# GitHub Wiki Setup Instructions

> 📘 **Source**: This file provides complete instructions for setting up the XAI-Forge GitHub Wiki

**Navigation**: [[Home]] > [[Reference & Resources]] > WIKI_SETUP

## Table of Contents

1. [Overview](#overview)
2. [Prerequisites](#prerequisites)
3. [GitHub Wiki Setup](#github-wiki-setup)
4. [Content Upload](#content-upload)
5. [Navigation Setup](#navigation-setup)
6. [Verification](#verification)
7. [Maintenance](#maintenance)
8. [Troubleshooting](#troubleshooting)

---

## Overview

This guide provides step-by-step instructions for setting up the XAI-Forge GitHub Wiki using the content created in the `docs/wiki/` directory. The wiki contains 32+ pages with complete documentation transferred from all existing project documentation.

### What You'll Get
- **Complete GitHub Wiki** with 32+ pages organized by logical sections
- **Professional Navigation** with sidebar and footer
- **100% Content Transfer** from existing documentation
- **Enhanced Organization** optimized for GitHub Wiki with numbered prefixes
- **Cross-linking** between related pages

---

## Prerequisites

### Required Accounts
- **GitHub Account**: With access to the xai-forge repository
- **Repository Access**: Admin or write access to the repository

### Required Tools
- **Git**: For cloning and pushing changes
- **Text Editor**: For any content modifications
- **Web Browser**: For accessing GitHub

### Repository Information
- **Repository**: `https://github.com/Mukaan17/xai-forge`
- **Wiki Location**: `https://github.com/Mukaan17/xai-forge/wiki`
- **Content Source**: `docs/wiki/` directory in the repository

---

## GitHub Wiki Setup

### Step 1: Enable GitHub Wiki

1. **Navigate to Repository**
   - Go to `https://github.com/Mukaan17/xai-forge`
   - Click on the repository

2. **Access Settings**
   - Click on the "Settings" tab
   - Scroll down to the "Features" section

3. **Enable Wiki**
   - Check the "Wikis" checkbox
   - Click "Save" to enable the wiki feature

4. **Verify Wiki Access**
   - Click on the "Wiki" tab in the repository navigation
   - You should see an empty wiki with a "Create the first page" button

### Step 2: Initialize Wiki Repository

1. **Clone Wiki Repository**
   ```bash
   git clone https://github.com/Mukaan17/xai-forge.wiki.git
   cd xai-forge.wiki
   ```

2. **Verify Empty State**
   - The wiki repository should be empty initially
   - This is normal for a new wiki

---

## Content Upload

### Step 3: Copy Wiki Content

1. **Copy All Files**
   ```bash
   # From the main repository directory
   cp -r docs/wiki/* xai-forge.wiki/
   ```

2. **Verify File Transfer**
   ```bash
   cd xai-forge.wiki
   ls -la
   # Should show all 28+ markdown files
   ```

### Step 4: Commit and Push

1. **Add All Files**
   ```bash
   git add .
   ```

2. **Commit Changes**
   ```bash
   git commit -m "Initial wiki setup with complete documentation

   - Added 28+ wiki pages with complete content transfer
   - Included navigation files (_Sidebar.md, _Footer.md)
   - Transferred all existing documentation with 100% preservation
   - Added enhanced cross-linking and organization
   - Ready for GitHub Wiki deployment"
   ```

3. **Push to GitHub**
   ```bash
   git push origin main
   ```

---

## Navigation Setup

### Step 5: Configure Sidebar

The `_Sidebar.md` file is automatically used by GitHub Wiki for navigation. It includes:

- **Getting Started** section with quick links
- **User Guide** with FAQ
- **Developer Documentation** with architecture and APIs
- **Architecture Decisions** with ADR links
- **Testing & Quality** with guides and coverage
- **Contributing & Development** with setup and standards
- **Operations & Deployment** with deployment and monitoring
- **Project Management** with status and roadmap
- **Reference & Resources** with technology stack and glossary

### Step 6: Configure Footer

The `_Footer.md` file provides consistent footer information across all pages:

- **License information**
- **Copyright notice**
- **Quick links to repository resources**
- **Last updated information**

---

## Verification

### Step 7: Verify Wiki Setup

1. **Check Wiki Homepage**
   - Go to `https://github.com/Mukaan17/xai-forge/wiki`
   - Verify the Home page loads correctly
   - Check that the sidebar navigation appears

2. **Test Navigation**
   - Click on various sidebar links
   - Verify pages load correctly
   - Check that cross-links work properly

3. **Verify Content**
   - Check that all 28+ pages are accessible
   - Verify content is complete and properly formatted
   - Test search functionality

4. **Check Footer**
   - Scroll to bottom of any page
   - Verify footer information appears
   - Test footer links

### Step 8: Test Key Pages

**Essential Pages to Verify:**
- [[Home]] - Main landing page
- [[01-Getting-Started-Quick-Start|Quick Start]] - Getting started guide
- [[04-User-Guide|User Guide]] - Complete user documentation
- [[07-Developer-API-Reference|API Reference]] - REST API documentation
- [[06-Developer-Architecture|Architecture]] - System architecture
- [[19-Contributing|Contributing]] - Contribution guidelines
- [[25-Project-Status|Project Status]] - Current project status

---

## Maintenance

### Step 9: Ongoing Maintenance

1. **Content Updates**
   - Update wiki content when main documentation changes
   - Keep version information current
   - Maintain cross-links and navigation

2. **Regular Verification**
   - Check that all links work
   - Verify content accuracy
   - Update last modified dates

3. **User Feedback**
   - Monitor wiki usage and feedback
   - Improve content based on user needs
   - Add new pages as needed

### Step 10: Content Synchronization

**When to Update Wiki:**
- After major documentation changes
- When adding new features
- After resolving issues
- When updating project status

**How to Update:**
```bash
# Pull latest changes
git pull origin main

# Copy updated content
cp -r docs/wiki/* xai-forge.wiki/

# Commit and push
git add .
git commit -m "Update wiki content"
git push origin main
```

---

## Troubleshooting

### Common Issues

#### Issue: Wiki not enabled
**Solution:**
- Check repository settings
- Ensure you have admin access
- Verify wiki feature is enabled

#### Issue: Files not appearing
**Solution:**
- Check file permissions
- Verify git push was successful
- Refresh the wiki page

#### Issue: Navigation not working
**Solution:**
- Verify `_Sidebar.md` exists
- Check file naming (must be exactly `_Sidebar.md`)
- Ensure proper markdown formatting

#### Issue: Cross-links broken
**Solution:**
- Check page names match exactly
- Verify markdown link syntax
- Test links manually

### Advanced Troubleshooting

#### Git Issues
```bash
# Check git status
git status

# Check remote configuration
git remote -v

# Force push if needed (use with caution)
git push origin main --force
```

#### Content Issues
```bash
# Verify file contents
cat _Sidebar.md

# Check file permissions
ls -la

# Validate markdown syntax
# Use online markdown validators
```

---

## Success Criteria

### Wiki Setup Complete When:
- [ ] All 32+ pages are accessible
- [ ] Sidebar navigation works correctly
- [ ] Footer appears on all pages
- [ ] Cross-links function properly
- [ ] Search works for content
- [ ] All content is properly formatted
- [ ] No broken links or missing pages
- [ ] Logical organization with numbered prefixes is clear

### Quality Checklist:
- [ ] Professional appearance
- [ ] Consistent formatting
- [ ] Complete content transfer
- [ ] Enhanced navigation
- [ ] Proper cross-linking
- [ ] Updated information
- [ ] Working search functionality

---

## Additional Resources

### GitHub Wiki Documentation
- [GitHub Wiki Help](https://help.github.com/en/github/building-a-strong-community/about-wikis)
- [Wiki Markdown Guide](https://guides.github.com/features/mastering-markdown/)

### XAI-Forge Resources
- [Main Repository](https://github.com/Mukaan17/xai-forge)
- [Issues](https://github.com/Mukaan17/xai-forge/issues)
- [Discussions](https://github.com/Mukaan17/xai-forge/discussions)

### Support
- Check the [[Troubleshooting]] page for common issues
- Review the [[FAQ]] for frequently asked questions
- Contact the project maintainer for additional help

---

**Congratulations!** You now have a complete, professional GitHub Wiki for the XAI-Forge project with 28+ pages of comprehensive documentation.

---

**Next**: [[Home]] | **Previous**: [[Glossary]]  
**Related**: [[Project Status|Project-Status]], [[Contributing]], [[Architecture]]
