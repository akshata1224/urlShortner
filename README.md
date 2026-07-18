vscode ➜ /workspaces/urlShortner (main) $ curl -X POST http://localhost:8080/api/v1/urls \
  -H 'Content-Type: application/json' \
  -d '{"url":"https://example.com","customAlias":"Akash"}'
{"customAlias":"akash","shortCode":"baaaaaa","originalUrl":"https://example.com","shortUrl":"http://localhost:8080/akash/baaaaaa","createdAt":"2026-07-18T07:06:24.589960491Z","expiresAt":"2026-07-19T07:06:24.589960491Z","accessCount":0}vscode ➜ /workspaces/urlShorvscode ➜ /workspaces/urlShortner (main) $ 

vscode ➜ /workspaces/urlShortner (main) $ curl -i http://localhost:8080/akash/baaaaaa
HTTP/1.1 302 
Location: https://example.com
Cache-Control: no-cache
Content-Length: 0
Date: Sat, 18 Jul 2026 07:10:20 GMT


<!-- 1. THE WRITE PATH: POST /api/v1/urls -->

<!-- User Payload ──► [UrlShortenerController] ──► Passes payload to service layer
                                                     │
                                                     ▼
                                        [UrlShortenerService]
                                                     │
                                   1. Grab Next Atomic ID (O(1) CAS Step)
                                   2. Run Base62 Math (Convert ID to 7-char code)
                                   3. Evaluate Custom Alias Presence
                                                     │
                                   ┌─────────────────┴─────────────────┐
                       Has Custom Alias? (YES)              Has Custom Alias? (NO)
                                   │                                   │
                                   ▼                                   ▼
                       Build Branded String Code            Build Pure Direct Code
                     (e.g., bit.ly/akash/baaaaaa)             (e.g., bit.ly/baaaaaa)
                                   │                                   │
                                   └─────────────────┬─────────────────┘
                                                     │
                                                     ▼
                                      [InMemoryUrlRepository]
                                                     │
                                  Executes Atomic storage.putIfAbsent()
                                                     │
                                                     ▼
                                  Returns JSON Response payload to user -->


=======================================================================================
2. THE READ PATH: GET /{*remainingPath}
=======================================================================================

<!--
Browser Click ──► [UrlShortenerController] ──► Catches wildcard trailing segments
                                                     │
                                                     ▼
                                            [ShortUrlPath]
                                                     │
                                 1. Strip leading/trailing slashes ('/')
                                 2. Evaluate path depth layers (1 or 2 steps)
                                 3. Validate code matching ^[a-zA-Z0-9]{7}$ regex
                                                     │
                                                     ▼ (Extracted shortCode)
                                        [UrlShortenerService]
                                                     │
                                      [InMemoryUrlRepository]
                                                     │
                                    Queries map: storage.get(shortCode)
                                                     │
                                   ┌─────────────────┴─────────────────┐
                                Found?                              Not Found / Expired?
                                   │                                   │
                                   ▼                                   ▼
                        1. Increment Click Count               Throw Custom Exception
                        2. Fetch Original Long URL             Mapped to HTTP 404/410
                                   │
                                   ▼
                        [UrlShortenerController]
                                   │
                                   ▼
                        Returns HTTP 302 Redirect
                        Cache-Control: no-cache  -- >



 <!-- uniqueIdGenerator.getAndIncrement() ]
                 │
                 ▼
         Get Numeric ID 
     (e.g., 56,800,235,584)
                 │
                 ▼
     ┌───────────────────────┐
     │  UrlGenerator.encode  │
     └───────────────────────┘
                 │
                 ▼
       Loop while ID > 0:
       ┌────────────────────────────────────────────────────────┐
       │ 1. Remainder = ID % 62                                 │
       │ 2. Find Character: BASE62_CHARACTERS.charAt(Remainder) │
       │ 3. Append Character to StringBuilder                   │
       │ 4. ID = ID / 62                                        │
       └────────────────────────────────────────────────────────┘
                 │
                 ▼
     [ Reverse StringBuilder ]
                 │
                 ▼
     ┌───────────────────────┐
     │  padLeft(Result, 7)   │
     └───────────────────────┘
                 │
                 ▼
         Final Shortcode 
          (e.g., "baaaaaa")      -->            
