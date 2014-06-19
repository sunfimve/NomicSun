package facts;

import java.util.ArrayList;


/**
 * manage multiplicative modifiers on agent harvest rate or replenishment rate.
 * @author Hanguang Sun
 *
 */
public class ModifierManager {
	public class Modifier {
		private float value;
		private int duration;
		private boolean inEffect;
		public Modifier(float v, int d){
			value = v;
			duration = d;
			if (duration > 0){
				inEffect = true;
			}
		}
		
		public float getValue(){
			return value;
		}
		
		public void decrementTimer(){
			--duration;
			if (duration <= 0){
				inEffect = false;
				
			}
			}
		public boolean isinEffect(){
			return inEffect;
		}
		
	}
	
	
	public ArrayList<Modifier> ModifierSet;
	
	public float getOverallModifier(){
		float r = 1;
		float p = 0;
		float n = 0;
		float s = 1;
		if(ModifierSet != null){
			for (Modifier m : ModifierSet){
				if(m.getValue() > 1){
					p = p + m.getValue() - 1;
				}
				else if(m.getValue() < 1 && m.getValue() > 0){
					n = n + 1 / m.getValue() - 1;
				}
				else {
					s = 0;
				}
			}
			r = r * (1 + p) / (1 + n) * s;
				
		}
		return r;
	}
	
	public void insertmodifier(float mult, int time){
		ModifierSet.add( new Modifier(mult,time) );
	}
	
	public synchronized void decrementModifier(){
		if (ModifierSet != null){
			for (Modifier m : this.ModifierSet){
			m.decrementTimer();
			if (m.isinEffect() == false){
				this.ModifierSet.remove(m);
				}
			}
		}
		else{
			System.out.println("no modifier for this unit");
		}
	}

	public ModifierManager() {
		super();
		this.ModifierSet = new ArrayList<Modifier>();
	}
	
	public boolean isEmpty(){
		return ModifierSet == null;
	}

	
}
