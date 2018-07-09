package valandur.webapi.cache.data;

import io.swagger.annotations.ApiModel;
import io.swagger.annotations.ApiModelProperty;
import org.spongepowered.api.data.manipulator.mutable.entity.ExperienceHolderData;
import valandur.webapi.cache.CachedObject;

@ApiModel("ExperienceHolderData")
public class CachedExperienceHolderData extends CachedObject<ExperienceHolderData> {

    @ApiModelProperty("The current level of the entity")
    public int level;

    @ApiModelProperty("The experience gained since the last level")
    public int experience;

    @ApiModelProperty("The total amount of experience collected")
    public int totalExperience;


    public CachedExperienceHolderData(ExperienceHolderData value) {
        super(value);

        this.level = value.level().get();
        this.experience = value.experienceSinceLevel().get();
        this.totalExperience = value.totalExperience().get();
    }
}