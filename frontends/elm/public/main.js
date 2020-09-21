// Initial data passed to Elm (should match `Flags` defined in `Shared.elm`)
// https://guide.elm-lang.org/interop/flags.html
let flags = [
    "http://localhost:8080/json", // backendUrl
    1000                      // REST API timeout
];

// Start our Elm application
let app = Elm.Main.init({flags: flags});

// Ports go here
// https://guide.elm-lang.org/interop/ports.html