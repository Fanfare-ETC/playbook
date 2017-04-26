import UIKit
import WebKit

class CollectionController: UIViewController {
    
    @IBOutlet var containerView: UIView!
    var webView: WKWebView?
    
    override func loadView() {
        super.loadView()
        
        let contentController = WKUserContentController()
        let config = WKWebViewConfiguration()
        config.userContentController = contentController
        config.preferences.setValue(true, forKey: "allowFileAccessFromFileURLs")
        
        self.webView = WKWebView(
            frame: self.containerView.bounds,
            configuration: config
        )
        
        view = self.webView
    }
    
    override func viewDidLoad() {
        super.viewDidLoad()
        
        let filePath = Bundle.main.resourcePath?.appending("/Assets/collection/index.html")
        let url = URL(fileURLWithPath: filePath!)
        self.webView?.loadFileURL(url, allowingReadAccessTo: url)
    }
    
    override func didReceiveMemoryWarning() {
        super.didReceiveMemoryWarning()
    }
}
