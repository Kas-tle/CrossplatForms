package dev.projectg.crossplatforms.form.bedrock.custom;

import lombok.Getter;
import org.geysermc.cumulus.component.InputComponent;
import org.spongepowered.configurate.objectmapping.ConfigSerializable;

import java.util.function.Function;

@Getter
@ConfigSerializable
@SuppressWarnings("FieldMayBeFinal")
public class Input extends Component implements InputComponent {

    private String placeholder = "";
    private String defaultText = "";

    @Override
    public Component withPlaceholders(Function<String, String> resolver) {
        Input input = new Input();
        input.type = this.type;
        input.text = resolver.apply(this.text);
        input.defaultText = resolver.apply(this.defaultText);
        input.placeholder = resolver.apply(this.placeholder);
        return input;
    }
}