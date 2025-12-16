# @Date:   2025-10-24 12:11:37
# @Last Modified by:   Mukhil Sundararaj
# @Last Modified time: 2025-10-24 18:35:42
#!/bin/bash

# XAI-Forge Build Script
# This script builds the entire application for different environments

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
BUILD_DIR="$PROJECT_ROOT/build"
TIMESTAMP=$(date +"%Y%m%d_%H%M%S")

echo -e "${BLUE}=== XAI-Forge Build Script ===${NC}"
echo "Project Root: $PROJECT_ROOT"
echo "Timestamp: $TIMESTAMP"
echo ""

# Function to build backend
build_backend() {
    echo -e "${YELLOW}Building Backend...${NC}"
    cd "$BACKEND_DIR"
    
    # Clean previous builds
    mvn clean
    
    # Compile and package
    mvn compile package -DskipTests
    
    # Create JAR file
    if [ -f "target/backend-1.0.0.jar" ]; then
        echo -e "${GREEN}Backend build successful${NC}"
        
        # Copy JAR to build directory
        mkdir -p "$BUILD_DIR"
        cp target/backend-1.0.0.jar "$BUILD_DIR/xai-backend-$TIMESTAMP.jar"
        
        echo "Backend JAR: $BUILD_DIR/xai-backend-$TIMESTAMP.jar"
    else
        echo -e "${RED}Backend build failed${NC}"
        return 1
    fi
}

# Function to build frontend
build_frontend() {
    echo -e "${YELLOW}Building Frontend...${NC}"
    cd "$FRONTEND_DIR"
    
    # Install dependencies if needed
    if [ ! -d "node_modules" ]; then
        echo "Installing frontend dependencies..."
        npm install
    fi
    
    # Build production bundle
    npm run build
    
    # Check if build was successful
    if [ -d "build" ]; then
        echo -e "${GREEN}Frontend build successful${NC}"
        
        # Copy build to build directory
        mkdir -p "$BUILD_DIR/frontend"
        cp -r build/* "$BUILD_DIR/frontend/"
        
        echo "Frontend build: $BUILD_DIR/frontend/"
    else
        echo -e "${RED}Frontend build failed${NC}"
        return 1
    fi
}

# Function to create production build
build_production() {
    echo -e "${YELLOW}Building Production Version...${NC}"
    
    # Build backend with production profile
    cd "$BACKEND_DIR"
    mvn clean package -Pprod -DskipTests
    
    # Build frontend for production
    cd "$FRONTEND_DIR"
    npm run build:prod
    
    echo -e "${GREEN}Production build completed${NC}"
}

# Function to create Docker images
build_docker() {
    echo -e "${YELLOW}Building Docker Images...${NC}"
    
    # Build backend Docker image
    cd "$BACKEND_DIR"
    docker build -t xai-backend:$TIMESTAMP .
    
    # Build frontend Docker image
    cd "$FRONTEND_DIR"
    docker build -t xai-frontend:$TIMESTAMP .
    
    echo -e "${GREEN}Docker images built successfully${NC}"
    echo "Backend image: xai-backend:$TIMESTAMP"
    echo "Frontend image: xai-frontend:$TIMESTAMP"
}

# Function to create deployment package
create_deployment_package() {
    echo -e "${YELLOW}Creating Deployment Package...${NC}"
    
    # Create deployment directory
    DEPLOY_DIR="$BUILD_DIR/deployment-$TIMESTAMP"
    mkdir -p "$DEPLOY_DIR"
    
    # Copy backend JAR
    cp "$BUILD_DIR/xai-backend-$TIMESTAMP.jar" "$DEPLOY_DIR/"
    
    # Copy frontend build
    cp -r "$BUILD_DIR/frontend" "$DEPLOY_DIR/"
    
    # Copy configuration files
    cp -r config "$DEPLOY_DIR/"
    cp -r scripts "$DEPLOY_DIR/"
    cp setup-database.sql "$DEPLOY_DIR/"
    cp start.sh "$DEPLOY_DIR/"
    
    # Copy documentation
    mkdir -p "$DEPLOY_DIR/docs"
    cp -r docs/* "$DEPLOY_DIR/docs/"
    cp README.md "$DEPLOY_DIR/"
    cp LICENSE "$DEPLOY_DIR/"
    
    # Create deployment script
    cat > "$DEPLOY_DIR/deploy.sh" << 'EOF'
#!/bin/bash
# XAI-Forge Deployment Script

set -e

echo "Starting XAI-Forge deployment..."

# Check prerequisites
if ! command -v java &> /dev/null; then
    echo "Error: Java is not installed"
    exit 1
fi

if ! command -v psql &> /dev/null; then
    echo "Error: PostgreSQL is not installed"
    exit 1
fi

# Setup database
echo "Setting up database..."
psql -U postgres -f setup-database.sql

# Start backend
echo "Starting backend server..."
java -jar xai-backend-*.jar &

# Wait for backend to start
sleep 30

# Start frontend (if nginx is available)
if command -v nginx &> /dev/null; then
    echo "Configuring nginx for frontend..."
    # Add nginx configuration here
fi

echo "Deployment completed successfully!"
EOF
    
    chmod +x "$DEPLOY_DIR/deploy.sh"
    
    # Create tar.gz package
    cd "$BUILD_DIR"
    tar -czf "xai-deployment-$TIMESTAMP.tar.gz" "deployment-$TIMESTAMP"
    
    echo -e "${GREEN}Deployment package created${NC}"
    echo "Package: $BUILD_DIR/xai-deployment-$TIMESTAMP.tar.gz"
}

# Function to run tests before build
run_tests() {
    echo -e "${YELLOW}Running Tests Before Build...${NC}"
    
    # Run backend tests
    cd "$BACKEND_DIR"
    mvn test
    
    # Run frontend tests
    cd "$FRONTEND_DIR"
    npm test -- --watchAll=false
    
    echo -e "${GREEN}All tests passed${NC}"
}

# Function to clean build artifacts
clean_build() {
    echo -e "${YELLOW}Cleaning Build Artifacts...${NC}"
    
    # Clean backend
    cd "$BACKEND_DIR"
    mvn clean
    
    # Clean frontend
    cd "$FRONTEND_DIR"
    rm -rf build node_modules/.cache
    
    # Clean build directory
    rm -rf "$BUILD_DIR"
    
    echo -e "${GREEN}Build artifacts cleaned${NC}"
}

# Main execution
main() {
    echo -e "${BLUE}Starting build process...${NC}"
    
    # Parse command line arguments
    BUILD_BACKEND=true
    BUILD_FRONTEND=true
    RUN_TESTS=true
    CREATE_PACKAGE=false
    BUILD_DOCKER=false
    CLEAN_FIRST=false
    
    while [[ $# -gt 0 ]]; do
        case $1 in
            --backend-only)
                BUILD_FRONTEND=false
                shift
                ;;
            --frontend-only)
                BUILD_BACKEND=false
                shift
                ;;
            --no-tests)
                RUN_TESTS=false
                shift
                ;;
            --package)
                CREATE_PACKAGE=true
                shift
                ;;
            --docker)
                BUILD_DOCKER=true
                shift
                ;;
            --clean)
                CLEAN_FIRST=true
                shift
                ;;
            --help)
                echo "Usage: $0 [options]"
                echo "Options:"
                echo "  --backend-only    Build only backend"
                echo "  --frontend-only   Build only frontend"
                echo "  --no-tests       Skip running tests"
                echo "  --package        Create deployment package"
                echo "  --docker         Build Docker images"
                echo "  --clean          Clean before building"
                echo "  --help           Show this help message"
                exit 0
                ;;
            *)
                echo "Unknown option: $1"
                echo "Use --help for usage information"
                exit 1
                ;;
        esac
    done
    
    # Clean if requested
    if [ "$CLEAN_FIRST" = true ]; then
        clean_build
    fi
    
    # Run tests if requested
    if [ "$RUN_TESTS" = true ]; then
        run_tests
    fi
    
    # Build components
    if [ "$BUILD_BACKEND" = true ]; then
        build_backend
    fi
    
    if [ "$BUILD_FRONTEND" = true ]; then
        build_frontend
    fi
    
    # Build Docker images if requested
    if [ "$BUILD_DOCKER" = true ]; then
        build_docker
    fi
    
    # Create deployment package if requested
    if [ "$CREATE_PACKAGE" = true ]; then
        create_deployment_package
    fi
    
    echo ""
    echo -e "${GREEN}=== Build Complete ===${NC}"
    echo "Build artifacts in: $BUILD_DIR"
    echo "Timestamp: $TIMESTAMP"
    
    # List generated files
    if [ -d "$BUILD_DIR" ]; then
        echo ""
        echo "Generated files:"
        find "$BUILD_DIR" -type f -name "*$TIMESTAMP*" 2>/dev/null || echo "No files found with timestamp $TIMESTAMP"
    fi
}

# Run main function with all arguments
main "$@"
