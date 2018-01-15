package valandur.webapi.serialize.view.event;

import com.fasterxml.jackson.annotation.JsonAnyGetter;
import com.fasterxml.jackson.annotation.JsonProperty;
import org.spongepowered.api.event.Event;
import org.spongepowered.api.event.achievement.GrantAchievementEvent;
import org.spongepowered.api.event.block.TargetBlockEvent;
import org.spongepowered.api.event.entity.TargetEntityEvent;
import org.spongepowered.api.event.entity.living.humanoid.HandInteractEvent;
import org.spongepowered.api.event.entity.living.humanoid.player.KickPlayerEvent;
import org.spongepowered.api.event.item.inventory.InteractInventoryEvent;
import org.spongepowered.api.event.user.BanUserEvent;
import org.spongepowered.api.event.user.TargetUserEvent;
import org.spongepowered.api.event.world.ExplosionEvent;
import org.spongepowered.api.event.world.GenerateChunkEvent;
import org.spongepowered.api.event.world.TargetWorldEvent;
import valandur.webapi.api.block.IBlockOperationEvent;
import valandur.webapi.api.serialize.BaseView;

import java.util.HashMap;
import java.util.Map;

public class EventView extends BaseView<Event> {

    @JsonProperty(value = "class")
    public String clazz;

    private Map<String, Object> data = new HashMap<>();
    @JsonAnyGetter
    public Map<String, Object> getData() {
        return data;
    }


    public EventView(Event value) {
        super(value);

        this.clazz = value.getClass().getName();

        if (value instanceof TargetEntityEvent) {
            data.put("target", ((TargetEntityEvent)value).getTargetEntity());
        } else if (value instanceof TargetUserEvent) {
            data.put("target", ((TargetUserEvent)value).getTargetUser());
        }

        if (value instanceof KickPlayerEvent) {
            data.put("message", ((KickPlayerEvent)value).getMessage());
        }

        if (value instanceof BanUserEvent) {
            data.put("ban", ((BanUserEvent)value).getBan());
        }

        if (value instanceof GrantAchievementEvent) {
            data.put("achievement", ((GrantAchievementEvent)value).getAchievement());
        }

        if (value instanceof InteractInventoryEvent) {
            data.put("inventory", ((InteractInventoryEvent)value).getTargetInventory());
        }

        if (value instanceof IBlockOperationEvent) {
            data.put("operation", ((IBlockOperationEvent)value).getBlockOperation());
        }

        if (value instanceof GenerateChunkEvent) {
            data.put("chunk", ((GenerateChunkEvent)value).getTargetChunk());
        }

        if (value instanceof ExplosionEvent) {
            data.put("explosion", ((ExplosionEvent)value).getExplosion());
        }

        if (value instanceof TargetWorldEvent) {
            data.put("world", ((TargetWorldEvent)value).getTargetWorld());
        }

        if (value instanceof TargetBlockEvent) {
            data.put("block", ((TargetBlockEvent)value).getTargetBlock());
        }

        if (value instanceof HandInteractEvent) {
            data.put("hand", ((HandInteractEvent)value).getHandType());
        }

        try {
            data.put("cause", value.getCause());
        } catch (AbstractMethodError ignored) {}
    }
}
