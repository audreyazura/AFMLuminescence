/*
 * Copyright (C) 2020-2021 Alban Lafuente
 *
 * This program is free software: you can redistribute it and/or modify
 * it under the terms of the GNU General Public License as published by
 * the Free Software Foundation, either version 3 of the License, or
 * (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */
package afmluminescence.guimanager;

import java.math.BigDecimal;
import javafx.scene.paint.Color;

/**
 *
 * @author Alban Lafuente
 */
public class ObjectToDraw
{
    private final BigDecimal m_xPosition;
    private final BigDecimal m_yPosition;
    private final Color m_objectColor;
    private final double m_radius;
    
    public ObjectToDraw (BigDecimal x, BigDecimal y, Color p_paintColor, double radius)
    {
        m_xPosition = x;
        m_yPosition = y;
        m_objectColor = p_paintColor;
        m_radius = radius;
    }
    
    public BigDecimal getX()
    {
        return m_xPosition;
    }
    
    public BigDecimal getY()
    {
        return m_yPosition;
    }
    
    public Color getColor()
    {
        return m_objectColor;
    }
    
    public double getRadius()
    {
        return m_radius;
    }
}
