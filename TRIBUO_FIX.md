# Tribuo Dependency Fix

## Issue
The `org.tribuo.classification` and `org.tribuo.regression` packages are not found when using only `tribuo-core:4.3.2`.

## Solution Options

1. **Check if packages are in tribuo-core**: The classification and regression classes might be in tribuo-core but with different package structure
2. **Add Maven Repository**: Tribuo might need a specific repository
3. **Use Different Version**: Try a different Tribuo version that includes these packages
4. **Use Fully Qualified Names**: Use fully qualified class names instead of imports

## Current Status
- Using fully qualified names (`org.tribuo.classification.Label`, `org.tribuo.regression.Regressor`)
- If this doesn't work, we may need to:
  - Add a Maven repository for Tribuo
  - Use a different Tribuo version
  - Check if these classes are in a different artifact

## Next Steps
1. Try compiling with fully qualified names
2. If still failing, check Tribuo documentation for correct dependencies
3. Consider using Tribuo 4.2.x or checking if there's a `tribuo-all` artifact

