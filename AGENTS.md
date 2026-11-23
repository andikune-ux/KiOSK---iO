# Kioskio — Agent Instructions

This repository is an Android application for maintaining and operating a 45-motor vending machine.

## Required rules
- Follow AI Rulebook V3: https://github.com/andikune-ux/ai-rulebook
- Read its AGENTS.md, all required core rules, and platforms/android/RULES.md before substantial work.
- Keep this project-specific identity: applicationId/namespace `dev.andikune.kioskio`.
- Do not apply NSiO-only versioning rules to Kioskio.
- Never claim a physical motor moved unless a real controller command and reliable acknowledgement/feedback prove it.
- Until the controller interface and protocol are identified, motor actions must remain disabled. Do not substitute simulated motor movement.
- Treat remote motor actuation and unauthenticated LAN access as safety-sensitive. Do not expose motor-control endpoints without an approved safety and access-control design.
- Keep signing material out of source control. Use protected CI secrets if signing is configured.
- Preserve user data; use atomic writes and bounded logs/downloads.
- Report tests/builds as PASS, FAIL, NOT RUN, or NOT APPLICABLE with evidence.

## Current hardware constraint
The terminal is a second-hand Android device. Controller model, wiring, transport, protocol, and feedback are not yet known. Inspect Android software and accessible interfaces first; do not assume ADB access proves access to the motor controller.

## Implementation discipline
Inspect before changing. Define acceptance criteria. Fix one active feature at a time, test it, audit the diff, then proceed. Document assumptions and blockers.
