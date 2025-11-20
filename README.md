# Herocraft

### Database & Deckbuilder

Herocraft is the unofficial database and deck builder for the Ivion Deck Building game by Luminary Games. 

[![Docker Image CI](https://github.com/sirlag/Herocraft/actions/workflows/docker-image.yml/badge.svg)](https://github.com/sirlag/Herocraft/actions/workflows/docker-image.yml)

## Features


## Design

Herocraft is designed in two pieces, the backend server and the web application. The backend handles database tasks and
any long term processing (image processing, deck aggregation, etc), while the front end is the user facing side, and
handles much of the actual builder logic.

### API Notes

- Comparison to Scryfall’s Card API: see docs/api-comparison-scryfall.md for how Herocrafter’s IvionCard maps to Scryfall concepts (card_faces, image_uris, oracle_id, all_parts, etc.), plus suggested optional enhancements.
- IvionCard domain model review: see docs/domain-model-review.md for a detailed assessment of the current model, pitfalls, and recommended adjustments.

### Back End

The backend is a kotlin application written with the ktor server library from jetbrains.
The backend has a few external dependencies, all of which are listed in the included docker-compose.yaml file.

* Postgres 16.x or 17.x 
  * [x] Required
  * Other versions may work, but have not been explicitly tested.
  * Primary database and storage for the application
* Redis or valkey 
  * [ ] Required
  * Technically optional, but requires a code change to disable redis session storage.
* Minio or an alternative s3 compatible object store
  * [ ] Required
  * The card image processing pipeline requires an s3 compatible bucket store for storing bulk images
* SMTP Server
  * [ ] Required
  * Notifications are designed to be sent by email, but can be disabled and logged to console.

### Front end

Add a writeup on the front end later. It's a sveltekit app.