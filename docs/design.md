# Design notes

The app is an offline Swing client. `FlashcardStore` persists card content and scheduler state in a TSV file with base64-url encoded text fields, written through a temporary file and atomic replacement. `ReviewScheduler` implements a small SM-2-inspired interval rule: Again retries tomorrow and lowers ease, Hard increases the interval gently, and Good uses 1 day, 3 days, then the ease multiplier.

The scheduler is an educational baseline, not a claim of cognitive-science optimality. Back up the data file before moving devices. CSV import intentionally creates new cards; it does not overwrite existing IDs or import scheduling metadata.
