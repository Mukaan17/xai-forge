# @Date:   2025-10-24 12:11:19
# @Last Modified by:   Mukhil Sundararaj
# @Last Modified time: 2025-10-24 18:35:46
#!/bin/bash

# XAI-Forge Test Runner Script
# This script runs all tests and generates Excel reports

set -e

# Colors for output
RED='\033[0;31m'
GREEN='\033[0;32m'
YELLOW='\033[1;33m'
BLUE='\033[0;34m'
NC='\033[0m' # No Color

# Configuration
PROJECT_ROOT="$(cd "$(dirname "${BASH_SOURCE[0]}")/.." && pwd)"
BACKEND_DIR="$PROJECT_ROOT/backend"
FRONTEND_DIR="$PROJECT_ROOT/frontend"
TEST_REPORTS_DIR="$PROJECT_ROOT/test-reports"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${BLUE}=== XAI-Forge Test Runner ===${NC}"
echo "Project Root: $PROJECT_ROOT"
echo "Timestamp: $TIMESTAMP"
echo ""

# Create test reports directory
mkdir -p "$TEST_REPORTS_DIR"

# Function to run backend tests
run_backend_tests() {
    echo -e "${YELLOW}Running Backend Tests...${NC}"
    cd "$BACKEND_DIR"
    
    # Run tests with Maven
    mvn clean test
    
    # Generate test report
    if [ -f "target/surefire-reports/TEST-*.xml" ]; then
        echo -e "${GREEN}Backend tests completed successfully${NC}"
        
        # Convert test results to Excel
        java -cp "target/classes:target/test-classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
             com.example.xaiapp.util.ExcelTestReporter \
             --input-dir target/surefire-reports \
             --output "$TEST_REPORTS_DIR/test-results-$TIMESTAMP.xlsx"
        
        echo "Test results exported to: $TEST_REPORTS_DIR/test-results-$TIMESTAMP.xlsx"
    else
        echo -e "${RED}Backend test execution failed${NC}"
        return 1
    fi
}

# Function to run frontend tests
run_frontend_tests() {
    echo -e "${YELLOW}Running Frontend Tests...${NC}"
    cd "$FRONTEND_DIR"
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        echo "Installing frontend dependencies..."
        npm install
    fi
    
    # Run tests
    npm test -- --coverage --watchAll=false
    
    echo -e "${GREEN}Frontend tests completed successfully${NC}"
}

# Function to generate coverage report
generate_coverage_report() {
    echo -e "${YELLOW}Generating Coverage Report...${NC}"
    cd "$BACKEND_DIR"
    
    # Run tests with coverage
    mvn jacoco:report
    
    # Convert coverage to Excel
    if [ -f "target/site/jacoco/jacoco.xml" ]; then
        java -cp "target/classes:target/test-classes:$(mvn dependency:build-classpath -q -Dmdep.outputFile=/dev/stdout)" \
             com.example.xaiapp.util.CoverageToExcelConverter \
             --input target/site/jacoco/jacoco.xml \
             --output "$TEST_REPORTS_DIR/coverage-report-$TIMESTAMP.xlsx"
        
        echo "Coverage report exported to: $TEST_REPORTS_DIR/coverage-report-$TIMESTAMP.xlsx"
    else
        echo -e "${RED}Coverage report generation failed${NC}"
        return 1
    fi
}

# Function to run integration tests
run_integration_tests() {
    echo -e "${YELLOW}Running Integration Tests...${NC}"
    cd "$BACKEND_DIR"
    
    # Run integration tests
    mvn verify -Pintegration-test
    
    echo -e "${GREEN}Integration tests completed successfully${NC}"
}

# Function to run end-to-end tests
run_e2e_tests() {
    echo -e "${YELLOW}Running End-to-End Tests...${NC}"
    
    # Start backend in background
    echo "Starting backend server..."
    cd "$BACKEND_DIR"
    mvn spring-boot:run > "$TEST_REPORTS_DIR/backend.log" 2>&1 &
    BACKEND_PID=$!
    
    # Wait for backend to start
    sleep 30
    
    # Start frontend in background
    echo "Starting frontend server..."
    cd "$FRONTEND_DIR"
    npm start > "$TEST_REPORTS_DIR/frontend.log" 2>&1 &
    FRONTEND_PID=$!
    
    # Wait for frontend to start
    sleep 30
    
    # Run E2E tests
    npm run test:e2e
    
    # Stop servers
    kill $BACKEND_PID $FRONTEND_PID
    
    echo -e "${GREEN}End-to-end tests completed successfully${NC}"
}

# Main execution
main() {
    echo -e "${BLUE}Starting test execution...${NC}"
    
    # Parse command line arguments
    RUN_BACKEND=true
    RUN_FRONTEND=true
    RUN_INTEGRATION=true
    RUN_E2E=false
    GENERATE_COVERAGE=true
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --backend-only)
                RUN_FRONTEND=false
                RUN_INTEGRATION=false
                RUN_E2E=false
                shift
                ;;
            --frontend-only)
                RUN_BACKEND=false
                RUN_INTEGRATION=false
                RUN_E2E=false
                shift
                ;;
            --integration-only)
                RUN_BACKEND=false
                RUN_FRONTEND=false
                RUN_E2E=false
                shift
                ;;
            --e2e-only)
                RUN_BACKEND=false
                RUN_FRONTEND=false
                RUN_INTEGRATION=false
                RUN_E2E=true
                shift
                ;;
            --no-coverage)
                GENERATE_COVERAGE=false
                shift
                ;;
            --help)
                echo "Usage: $0 [options]"
                echo "Options:"
                echo "  --backend-only     Run only backend tests"
                echo "  --frontend-only    Run only frontend tests"
                echo "  --integration-only Run only integration tests"
                echo "  --e2e-only        Run only end-to-end tests"
                echo "  --no-coverage     Skip coverage report generation"
                echo "  --help            Show this help message"
                exit 0
                ;;
            *)
                echo "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    # Run tests based on configuration
    if [ "$RUN_BACKEND" = true ]; then
        run_backend_tests
    fi
    
    if [ "$RUN_FRONTEND" = true ]; then
        run_frontend_tests
    fi
    
    if [ "$RUN_INTEGRATION" = true ]; then
        run_integration_tests
    fi
    
    if [ "$RUN_E2E" = true ]; then
        run_e2e_tests
    fi
    
    if [ "$GENERATE_COVERAGE" = true ]; then
        generate_coverage_report
    fi
    
    echo ""
    echo -e "${GREEN}=== Test Execution Complete ===${NC}"
    echo "Reports generated in: $TEST_REPORTS_DIR"
    echo "Timestamp: $TIMESTAMP"
    
    # List generated files
    if [ -d "$TEST_REPORTS_DIR" ]; then
        echo ""
        echo "Generated files:"
        ls -la "$TEST_REPORTS_DIR"/*"$TIMESTAMP"* 2>/dev/null || echo "No files found with timestamp $TIMESTAMP"
    fi
}

# Run main function with all arguments
main "$@"
