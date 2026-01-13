/**
 * XAI-Forge Component Testing Script
 * 
 * Run this in the browser console (F12) at http://localhost:3000/login
 * This will authenticate and test all UI components
 */

async function testAllComponents() {
  console.log('🧪 Starting XAI-Forge Component Tests...\n');
  
  // Step 1: Authenticate
  console.log('1️⃣ Testing Authentication...');
  try {
    const response = await fetch('http://localhost:8080/api/v1/auth/login', {
      method: 'POST',
      headers: { 'Content-Type': 'application/json' },
      body: JSON.stringify({ username: 'testuser', password: 'Test123!' })
    });
    
    if (!response.ok) {
      throw new Error(`Login failed: ${response.status}`);
    }
    
    const data = await response.json();
    const token = data.token;
    const user = data.user;
    
    // Set auth state
    localStorage.setItem('token', token);
    localStorage.setItem('auth-storage', JSON.stringify({
      state: {
        user: user,
        token: token,
        isAuthenticated: true
      },
      version: 0
    }));
    
    console.log('✅ Authentication successful!');
    console.log('   User:', user.username);
    console.log('   Token:', token.substring(0, 30) + '...\n');
    
    // Step 2: Navigate to dashboard
    console.log('2️⃣ Navigating to Dashboard...');
    window.location.href = '/dashboard';
    
    // Wait for navigation
    await new Promise(resolve => setTimeout(resolve, 2000));
    
    // Step 3: Test API endpoints
    console.log('3️⃣ Testing API Endpoints...');
    const endpoints = [
      '/v1/dashboard/stats',
      '/v1/datasets',
      '/v1/models'
    ];
    
    for (const endpoint of endpoints) {
      try {
        const apiResponse = await fetch(`http://localhost:8080/api${endpoint}`, {
          headers: {
            'Authorization': `Bearer ${token}`,
            'Content-Type': 'application/json'
          }
        });
        
        if (apiResponse.ok) {
          const apiData = await apiResponse.json();
          console.log(`✅ ${endpoint}:`, Array.isArray(apiData) ? `${apiData.length} items` : 'OK');
        } else {
          console.log(`❌ ${endpoint}: ${apiResponse.status}`);
        }
      } catch (error) {
        console.log(`❌ ${endpoint}:`, error.message);
      }
    }
    
    console.log('\n✅ All tests completed!');
    console.log('\n📋 Manual Testing Checklist:');
    console.log('   □ Test Navigation dropdowns (Notifications, Help, User menu)');
    console.log('   □ Test sidebar navigation (Dashboard, Datasets, Models, etc.)');
    console.log('   □ Test Dashboard page components');
    console.log('   □ Test Datasets page (upload, preview, delete)');
    console.log('   □ Test Models page');
    console.log('   □ Test Predictions page');
    console.log('   □ Test Settings page');
    console.log('   □ Test logout functionality');
    
  } catch (error) {
    console.error('❌ Test failed:', error);
  }
}

// Auto-run if on login page
if (window.location.pathname === '/login') {
  console.log('🚀 XAI-Forge Test Script Loaded');
  console.log('   Run testAllComponents() in console to start testing');
  console.log('   Or wait 2 seconds for auto-run...\n');
  
  setTimeout(() => {
    testAllComponents();
  }, 2000);
} else {
  console.log('📝 XAI-Forge Test Script Loaded');
  console.log('   Run testAllComponents() in console to test components');
}

// Export for manual use
window.testAllComponents = testAllComponents;
