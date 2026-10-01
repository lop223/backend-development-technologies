package net.lop223.helpdesksystem.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class CategoryRequest {

    @NotBlank(message = "Name must not be blank")
    @Size(max = 100)
    private String name;

    @Size(max = 500)
    private String description;
}