# Architecture Decision Records (ADRs)

This directory contains Architecture Decision Records for the XAI-Forge project.

## What are ADRs?

Architecture Decision Records (ADRs) are documents that capture important architectural decisions made during the development of a project. They provide context for why certain decisions were made and help future developers understand the reasoning behind the architecture.

## ADR Format

Each ADR follows this format:
- **Status**: Proposed, Accepted, Deprecated, or Superseded
- **Context**: The issue motivating this decision
- **Decision**: The change that we're proposing or have agreed to implement
- **Consequences**: What becomes easier or more difficult to do and any risks introduced by this change

## Current ADRs

- [ADR-001: Choice of Tribuo for Machine Learning](ADR-001-tribuo-ml-library.md)
- [ADR-002: JWT vs Session-based Authentication](ADR-002-jwt-authentication.md)
- [ADR-003: Async Processing for Model Training](ADR-003-async-model-training.md)
- [ADR-004: Excel Reporting for Test Results](ADR-004-excel-test-reporting.md)
- [ADR-005: Transaction Isolation Levels Strategy](ADR-005-transaction-isolation.md)
- [ADR-006: Design Patterns Implementation](ADR-006-design-patterns.md)

## How to Create an ADR

1. Copy the template: `cp ADR-template.md ADR-XXX-descriptive-name.md`
2. Fill in the template with your decision
3. Update this README to include the new ADR
4. Submit a pull request with the new ADR
