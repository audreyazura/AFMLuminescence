/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afmluminescence.luminescencegenerator;

import com.github.audreyazura.commonutils.PhysicsTools;
import java.math.BigDecimal;

/**
 *
 * @author audreyazura
 */
public interface DrawingSurface
{
    public void drawAbsorberObject(AbsorberObject p_objectToDraw, BigDecimal p_xScale, BigDecimal p_yScale, BigDecimal p_radius);
    
    public void reset();
}
