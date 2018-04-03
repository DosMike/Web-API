package valandur.webapi.serialize.view.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.spongepowered.api.data.manipulator.mutable.entity.HealthData;
import valandur.webapi.api.serialize.BaseView;

@ApiModel("HealthData")
public class HealthDataView extends BaseView<HealthData> {

    @ApiModelProperty(value = "The current health of the entity", required = true)
    public double current;

    @ApiModelProperty(value = "The maximum health of the entity", required = true)
    public double max;


    public HealthDataView(HealthData value) {
        super(value);

        this.current = value.health().get();
        this.max = value.maxHealth().get();
    }
}
