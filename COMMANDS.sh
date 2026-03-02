#!/bin/bash

# E-Learning Platform - Quick Commands Guide

echo "🎓 E-Learning Platform - Commands Reference"
echo "============================================"
echo ""

# Build commands
echo "📦 BUILD COMMANDS:"
echo "  # Build the application"
echo "  ./gradlew clean bootJar"
echo ""
echo "  # Build and run tests"
echo "  ./gradlew clean build"
echo ""

# Docker commands
echo "🐳 DOCKER COMMANDS:"
echo "  # Start all services"
echo "  docker compose up --build -d"
echo ""
echo "  # Stop all services"
echo "  docker compose down"
echo ""
echo "  # View logs"
echo "  docker compose logs -f app"
echo ""
echo "  # Check status"
echo "  docker compose ps"
echo ""
echo "  # Restart app"
echo "  docker compose restart app"
echo ""

# Access points
echo "🌐 ACCESS POINTS:"
echo "  # Landing page (sign in / register)"
echo "  http://localhost:8080/"
echo ""
echo "  # Login page"
echo "  http://localhost:8080/login"
echo ""
echo "  # Registration page"
echo "  http://localhost:8080/register"
echo ""
echo "  # Dashboard (after login)"
echo "  http://localhost:8080/ui"
echo ""
echo "  # Admin panel (admin only)"
echo "  http://localhost:8080/ui/admin"
echo ""

# Default credentials
echo "🔐 DEFAULT CREDENTIALS:"
echo "  Admin:       admin@elearning.com / admin123"
echo "  Instructor:  john.instructor@elearning.com / john123"
echo "  Student:     alice@elearning.com / alice123"
echo ""

# Services
echo "📊 SERVICES (Ports):"
echo "  MySQL:     localhost:3306"
echo "  Kafka:     localhost:9092"
echo "  Zookeeper: localhost:2181"
echo "  App:       localhost:8080"
echo ""

# Development
echo "🔧 DEVELOPMENT COMMANDS:"
echo "  # Full setup from scratch"
echo "  ./gradlew clean bootJar && docker compose up --build -d"
echo ""
echo "  # Quick restart"
echo "  docker compose restart app"
echo ""
echo "  # View application logs in real-time"
echo "  docker compose logs -f app"
echo ""
echo "  # Connect to MySQL"
echo "  mysql -h localhost -u root -p -D elearning_db"
echo ""

# Testing
echo "🧪 TESTING WORKFLOWS:"
echo "  1. Test Landing Page:"
echo "     Open http://localhost:8080/ in browser"
echo ""
echo "  2. Test Sign In:"
echo "     Click 'Sign In' → Use admin credentials"
echo ""
echo "  3. Test Registration:"
echo "     Click 'Create Account' → Fill form → Create"
echo ""
echo "  4. Test Admin Panel:"
echo "     Login as admin → Click 'Admin' in nav"
echo ""
echo "  5. Test Kafka:"
echo "     Login → Go to Courses → Publish course"
echo "     Check logs: docker compose logs -f app"
echo ""

# Quick start
echo "🚀 QUICK START:"
echo "  1. Build:  ./gradlew clean bootJar"
echo "  2. Deploy: docker compose up --build -d"
echo "  3. Wait:   ~15 seconds"
echo "  4. Access: http://localhost:8080/"
echo ""

echo "✅ All commands ready to use!"

