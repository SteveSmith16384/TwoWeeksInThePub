package twoweeks;

import com.scs.stevetech1.entities.PhysicalEntity;
import com.scs.stevetech1.shared.AbstractCollisionValidator;

public class TwoWeeksCollisionValidator extends AbstractCollisionValidator {

	public boolean canCollide(PhysicalEntity a, PhysicalEntity b) {
		return super.canCollide(a, b);
	}
}
