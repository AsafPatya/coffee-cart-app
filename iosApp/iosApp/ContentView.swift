import SwiftUI
import ComposeApp

/// Bridges the shared Compose UI (`MainViewController()` in `:composeApp`) into SwiftUI.
struct ContentView: UIViewControllerRepresentable {
    func makeUIViewController(context: Context) -> UIViewController {
        MainViewControllerKt.MainViewController()
    }

    func updateUIViewController(_ uiViewController: UIViewController, context: Context) {}
}
