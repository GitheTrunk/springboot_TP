package example.demo;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.StandardCopyOption;
import java.util.List;

import jakarta.validation.Valid;
import org.springframework.core.io.Resource;
import org.springframework.core.io.UrlResource;
import org.springframework.http.HttpHeaders;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.http.ResponseEntity;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.validation.BindingResult;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.ModelAttribute;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.multipart.MultipartFile;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.ResponseBody;

@Controller
public class ProductController {
    private ProductRepository productRepository;

    public ProductController(ProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    @GetMapping("/")
    public String getTemplate(Model model) {
        model.addAttribute("pageTitle", "Product List");
        model.addAttribute("content", "fragments/list_product");
        model.addAttribute("contentName", "list_product");
        model.addAttribute("products", productRepository.findAll());
        return "index";
    }

    @GetMapping("/createProduct")
    public String getProduct(Model model) {
        model.addAttribute("pageTitle", "Create Product");
        model.addAttribute("content", "fragments/form");
        model.addAttribute("contentName", "formFragment");
        model.addAttribute("productForm", new ProductDTO());
        return "index";
    }

    @PostMapping("/save-product")
    public String saveProduct(@Valid @ModelAttribute("productForm") ProductDTO productForm,
            BindingResult bindingResult,
            Model model,
            org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        
        // For new products, require image
        if (productForm.getId() == null) {
            MultipartFile img = productForm.getImage();
            if (img == null || img.isEmpty()) {
                bindingResult.rejectValue("image", "image.required", "Please upload an image for the product");
            }
        }
        
        if (bindingResult.hasErrors()) {
            model.addAttribute("content", "fragments/form");
            model.addAttribute("contentName", "formFragment");
            model.addAttribute("pageTitle", productForm.getId() == null ? "Create Product" : "Edit Product");
            return "index";
        }

        Product product;
        if (productForm.getId() != null) {
            product = productRepository.findById(productForm.getId())
                    .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + productForm.getId()));
        } else {
            product = new Product();
        }
        product.setName(productForm.getName());
        product.setDescription(productForm.getDescription());
        product.setPrice(productForm.getPrice());

        MultipartFile file = productForm.getImage();
        if (file != null && !file.isEmpty()) {
            try {
                Path uploads = Path.of("src/main/resources/static/uploads");
                Files.createDirectories(uploads);
                String filename = System.currentTimeMillis() + "_" + file.getOriginalFilename();
                Path target = uploads.resolve(filename);
                Files.copy(file.getInputStream(), target, StandardCopyOption.REPLACE_EXISTING);
                product.setImagePath("/uploads/" + filename);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }

        Product saved = productRepository.save(product);
        String message = productForm.getId() == null 
            ? "Product created (id=" + saved.getId() + ")"
            : "Product updated (id=" + saved.getId() + ")";
        redirectAttributes.addFlashAttribute("successMessage", message);
        return "redirect:/";
    }

    @GetMapping("/upload/{filename:.+}")
    public ResponseEntity<Resource> getFile(@PathVariable("filename") String filename) {
        Path filePath = Path.of("src/main/resources/static/uploads").resolve(filename);
        Resource resource;
        try {
            resource = new UrlResource(filePath.toUri());
            if (resource.exists() || resource.isReadable()) {
                return ResponseEntity.ok()
                        .header(HttpHeaders.CONTENT_DISPOSITION,
                                "attachment; filename=\"" + resource.getFilename() + "\"")
                        .body(resource);
            } else {
                return ResponseEntity.notFound().build();
            }
        } catch (Exception e) {
            return ResponseEntity.badRequest().build();
        }
    }

    @GetMapping("/product/edit/{id}")
    public String editProduct(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        ProductDTO productDTO = new ProductDTO();
        productDTO.setId(product.getId());
        productDTO.setName(product.getName());
        productDTO.setDescription(product.getDescription());
        productDTO.setPrice(product.getPrice());

        model.addAttribute("productForm", productDTO);
        model.addAttribute("pageTitle", "Edit Product");
        model.addAttribute("content", "fragments/form");
        model.addAttribute("contentName", "formFragment");
        return "index";
    }

    @PostMapping("/product/delete/{id}")
    public String deleteProduct(@PathVariable("id") Long id, Model model) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        if (product.getImagePath() != null) {
            try {
                Path imagePath = Path.of("src/main/resources/static").resolve(product.getImagePath().substring(1));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productRepository.delete(product);
        return "redirect:/";
    }

    @PostMapping("/delete-product")
    public String deleteProductById(@RequestParam("id") Long id,
                                    org.springframework.web.servlet.mvc.support.RedirectAttributes redirectAttributes) {
        Product product = productRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Invalid product Id:" + id));
        if (product.getImagePath() != null) {
            try {
                Path imagePath = Path.of("src/main/resources/static").resolve(product.getImagePath().substring(1));
                Files.deleteIfExists(imagePath);
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        productRepository.delete(product);
        redirectAttributes.addFlashAttribute("successMessage", "Product deleted");
        return "redirect:/";
    }

    @GetMapping("/api/products")
    @ResponseBody
    public List<Product> apiProducts() {
        return productRepository.findAll();
    }
}
