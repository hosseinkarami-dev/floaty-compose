# Contributing

## Development

Keep reusable APIs and platform-independent behavior in `floaty/src/commonMain`. Add deterministic state behavior tests in `floaty/src/commonTest`. Keep sample-only UI in `shared` and keep platform code out of common sources unless a real platform distinction requires it.

Before proposing a change, run the relevant checks:

```sh
./gradlew :floaty:allTests
./gradlew :floaty:compileAndroidMain :floaty:compileKotlinJvm :floaty:compileKotlinJs :floaty:compileKotlinWasmJs
```

On macOS, also compile the iOS targets. Report unavailable checks accurately. Update the README when public behavior or compatibility claims change.

For the library's current feature set and platform status, see [README](README.md), the [API guide](docs/API_GUIDE.md), and [platform support](docs/PLATFORM_SUPPORT.md). Release preparation and JitPack verification are in [docs/PUBLISHING.md](docs/PUBLISHING.md).

## API and versioning

Floaty is pre-release. Keep the public API small and source-friendly. Once releases begin, use semantic versioning and provide deprecation paths for breaking changes. Add binary API validation before declaring the API stable.

## Release checklist

- Review public API and KDoc.
- Run common tests and all available target compilations.
- Verify the demo on supported platforms and document platform-specific gaps.
- Update README and changelog.
- Set the chosen license and publication coordinates.
- Configure signing and repository credentials only through local/CI secrets.
- Publish a tagged release and verify its artifacts from a clean consumer project.
