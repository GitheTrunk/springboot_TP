package example.demo;

import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Pattern;
import jakarta.validation.constraints.Size;
import org.springframework.web.multipart.MultipartFile;

public class ProductDTO {
    private Long id;

    @NotBlank(message = "Product name must not be empty")
    @Size(min = 3, max = 50, message = "Product name must be between 3 and 50 characters")
    @Pattern(regexp = "^[A-Za-z0-9 ]+$", message = "Product name must contain only letters, numbers and spaces")
    private String name;

    private String description;

    @Min(value = 1, message = "Price must be at least 1")
    private double price;

    @NotNull(message = "Please upload an image for the product")
    private MultipartFile image;

    public Long getId() {
        return id;
    }
    
    public void setId(Long id) {
        this.id = id;
    }
    public ProductDTO() {
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public double getPrice() {
        return price;
    }

    public void setPrice(double price) {
        this.price = price;
    }

    public MultipartFile getImage() {
        return image;
    }

    public void setImage(MultipartFile image) {
        this.image = image;
    }

}
