package ARain;

import vgpackage.*;

public class OurExp extends SpriteExp {
	protected void processGravity() {
        if (lifeSpan < 800)
			super.processGravity();
	}
}
