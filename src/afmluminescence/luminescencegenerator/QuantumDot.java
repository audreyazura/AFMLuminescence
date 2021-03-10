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
package afmluminescence.luminescencegenerator;

import com.github.audreyazura.commonutils.PhysicsTools;
import com.github.kilianB.pcg.fast.PcgRSFast;
import java.math.BigDecimal;
import java.math.MathContext;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.nevec.rjm.BigDecimalMath;

/**
 *
 * @author Alban Lafuente
 */
public class QuantumDot extends AbsorberObject
{
    private final BigDecimal m_energy;
    private final BigDecimal m_radius;
    private boolean m_recombined = false;
    
    //ΔEg(InAs/GaAs) ~ 1.1 eV
    public QuantumDot (BigDecimal p_positionX, BigDecimal p_positionY, BigDecimal p_radius, BigDecimal p_height)
    {
        BigDecimal two = new BigDecimal("2");
        PhysicsTools.Materials material = PhysicsTools.Materials.INAS;
        
        m_positionX = p_positionX;
        m_positionY = p_positionY;
        
        m_radius = p_radius;
        BigDecimal equivalentSquareSide = m_radius.multiply(BigDecimalMath.sqrt(BigDecimalMath.pi(MathContext.DECIMAL128), MathContext.DECIMAL128));
        
        BigDecimal energyPlane = (energyParameter(equivalentSquareSide).divide(equivalentSquareSide, MathContext.DECIMAL128)).pow(2);
        BigDecimal energyHeight = (energyParameter(p_height).divide(p_height, MathContext.DECIMAL128)).pow(2);
        m_energy = material.getBaseBandgapSI().add((two.multiply(PhysicsTools.hbar.pow(2)).divide(material.getElectronEffectiveMassSI(), MathContext.DECIMAL128)).multiply((energyPlane.multiply(two)).add(energyHeight)));
        
        //at the moment, approximation of energy modeling the QD as a cube with side length L = (2*radius + height)/3
//        BigDecimal characteristicLength = ((p_radius.multiply(two)).add(p_height)).divide(three, MathContext.DECIMAL128);
//        m_energy = material.getBaseBandgapSI().add((three.multiply(PhysicsTools.hbar.pow(2)).multiply(pi.pow(2))).divide(two.multiply(material.getElectronEffectiveMassSI()).multiply(characteristicLength.pow(2)), MathContext.DECIMAL128));
    }
    
    /**
     * The capture probability depends on many parameters and demand to be further investigate.
     * At the moment, it is approximated as the overlapping between the QD and the circle containing the positions the electron can reach
     * See here for the calculation of the overlap: https://www.xarg.org/2016/07/calculate-the-intersection-area-of-two-circles/
     * It had to be slightly adapted with four different cases:
     *  - when the electron is entirely inside the QD (electronDistance + electronSpan < radius)
     *  - when the QD is entirely "inside" the electron (electronDistance + radius < electronSpan)
     *  - when the center of the QD is farther away from the electron position than the intersection between the QD limit and the electron span limit (electronDistance > sqrt(abs(radius^2 - electronSpan^2)))
     *  - when the center of the QD is closer from the electron position than the intersection between the QD limit and the electron span limit (electronDistance < sqrt(abs(radius^2 - electronSpan^2)))
     * @param p_RNG the random number generator
     * @param electronDistance the distance between the center of the QD and electron position
     * @param electronSpan the circle containing the position the electron can reach
     * @return whether the electron has been captured or not
     */
    synchronized public boolean capture(PcgRSFast p_RNG, BigDecimal electronDistance, BigDecimal electronSpan)
    {
        double captureProba = 0;
        
        //case if the electron is entirely inside the QD
        if (electronDistance.add(electronSpan).compareTo(m_radius) <= 0)
        {
            captureProba = 1;
        }
        else
        {
            //if the QD is entirely in the electron span
            if (electronDistance.add(m_radius).compareTo(electronSpan) <= 0)
            {
                captureProba = (m_radius.pow(2).divide(electronSpan.pow(2), MathContext.DECIMAL128)).doubleValue();
            }
            else
            {
                BigDecimal overlapArea = BigDecimal.ZERO;

                //we compare the distance to sqrt(abs(radius^2 - electronSpan^2))
                BigDecimal radiusDiff = BigDecimalMath.sqrt(((m_radius.pow(2)).subtract(electronSpan.pow(2))).abs(), MathContext.DECIMAL128);
                
                //if the QD center is farther away than the intersection points
                if (electronDistance.compareTo(radiusDiff) >= 0)
                {
                    //the base of the triangle for the calculation here is (electronSpan^2 + electronDistance^2 - QDradius^2) / (2 * electronDistance)
                    BigDecimal triangleBase = (electronSpan.pow(2).add(electronDistance.pow(2)).subtract(m_radius.pow(2))).divide(electronDistance.multiply(new BigDecimal("2")), MathContext.DECIMAL128);
                    
                    BigDecimal electronSlice = electronSpan.pow(2).multiply(BigDecimalMath.acos(triangleBase.divide(electronSpan, MathContext.DECIMAL128)));
                    BigDecimal QDSlice = m_radius.pow(2).multiply(BigDecimalMath.acos((electronDistance.subtract(triangleBase)).divide(m_radius, MathContext.DECIMAL128)));
                    BigDecimal triangleCorrection = electronDistance.multiply(BigDecimalMath.sqrt(electronSpan.pow(2).subtract(triangleBase.pow(2)), MathContext.DECIMAL128));
                    
                    overlapArea = electronSlice.add(QDSlice).subtract(triangleCorrection);
                }
                else
                {
                    //the base of the triangle for the calculation here is (electronSpan^2 - electronDistance^2 - QDradius^2) / (2 * electronDistance)
                    BigDecimal triangleBase = (electronSpan.pow(2).subtract(electronDistance.pow(2)).subtract(m_radius.pow(2))).divide(electronDistance.multiply(new BigDecimal("2")), MathContext.DECIMAL128);
                    
                    BigDecimal electronSlice = electronSpan.pow(2).multiply(BigDecimalMath.acos((triangleBase.add(electronDistance)).divide(electronSpan, MathContext.DECIMAL128)));
                    BigDecimal QDSlice = m_radius.pow(2).multiply(BigDecimalMath.pi(MathContext.DECIMAL128).subtract(BigDecimalMath.acos(triangleBase.divide(m_radius, MathContext.DECIMAL128))));
                    BigDecimal triangleCorrection = electronDistance.multiply(BigDecimalMath.sqrt(m_radius.pow(2).subtract(triangleBase.pow(2)), MathContext.DECIMAL128));
                    
                    overlapArea = electronSlice.add(QDSlice).subtract(triangleCorrection);
                }
                
                captureProba = overlapArea.divide(BigDecimalMath.pi(MathContext.DECIMAL128).multiply(electronSpan.pow(2)), MathContext.DECIMAL128).doubleValue();
            }
        }
        
        if (captureProba < 0 || captureProba > 1)
        {
            System.out.println("Probability has to be bound between 0 and 1");
            Logger.getLogger(QuantumDot.class.getName()).log(Level.SEVERE, null, new ArithmeticException("Probability has to be bound between 0 and 1"));
        }
        
        return p_RNG.nextDouble() < captureProba;
    }
    
    private BigDecimal energyParameter (BigDecimal size)
    {
        BigDecimal two = new BigDecimal(2);
        BigDecimal halfpi = BigDecimalMath.pi(MathContext.DECIMAL128).divide(two, MathContext.DECIMAL128);
        BigDecimal CBOffset = (new BigDecimal("0.7")).multiply(PhysicsTools.EV); //from https://aip.scitation.org/doi/abs/10.1063/1.125965, make it into PhysicalTools as a new enum, Metamaterials
        BigDecimal u02 = PhysicsTools.Materials.INAS.getElectronEffectiveMassSI().multiply(size.pow(2)).multiply(CBOffset).divide(two.multiply(PhysicsTools.hbar.pow(2)), MathContext.DECIMAL128);
        
//        BigDecimal vi = new BigDecimal(Math.random()*Math.PI);
//        BigDecimal error = new BigDecimal("1e-10");
//        
//        BigDecimal vprev = BigDecimal.ZERO;
//        int counter = 0;
//        do
//        {
//            System.out.println(counter);
//            vprev = vi;
//            
//            BigDecimal fvi = (vi.pow(2).multiply(BigDecimal.ONE.add(BigDecimalMath.tan(vi).pow(2)))).subtract(u02);
//            System.out.println(counter);
//            BigDecimal fderivvi = two.multiply(vi).multiply(BigDecimal.ONE.add(vi.multiply(BigDecimalMath.tan(vi)).divide(BigDecimalMath.cos(vi).pow(2), MathContext.DECIMAL128)).add(BigDecimalMath.tan(vi).pow(2)));
//            
//            vi = (vprev.subtract(fvi.divide(fderivvi, MathContext.DECIMAL128))).abs();
//            while (vi.compareTo(halfpi) >= 0)
//            {
//                //vi has to be between 0 and pi/2
//                vi = vi.subtract(halfpi);
//            }
//            
//            counter += 1;
//            System.out.println(counter);
//        }while(counter <= 10 && (vprev.subtract(vi)).abs().compareTo(error) >= 0);
        
        double vi = Math.random()*Math.PI;
        double error = 1E-10;
        
        double vprev = 0;
        int counter = 0;
        do
        {
            vprev = vi;
            
            double fvi = Math.pow(vi, 2) * (1 + Math.pow(Math.tan(vi), 2)) - u02.doubleValue();
            double fderivvi = 2 * vi * (1 + (vi * Math.tan(vi) / (Math.pow(Math.cos(vi), 2))) + Math.pow(Math.tan(vi), 2));
            
            vi = Math.abs(vprev - (fvi / fderivvi));
            while (vi >= halfpi.doubleValue())
            {
                //vi has to be between 0 and pi/2
                vi = vi - halfpi.doubleValue();
            }
            
            counter += 1;
        }while(counter <= 10 && Math.abs(vprev - vi) >= error);

        return new BigDecimal(vi);
    }
    
    //will calculate probability based on phonon density
    synchronized public boolean escape(PcgRSFast p_RNG)
    {
        return p_RNG.nextDouble() < 0.01;
    }
    
    public BigDecimal getEnergy()
    {
        return m_energy;
    }
    
    public BigDecimal getRadius()
    {
        return m_radius;
    }
    
    public boolean hasRecombined()
    {
        return m_recombined;
    }
    
    //will calculate the probablity based on the electron and hole wave function
    synchronized public boolean recombine(PcgRSFast p_RNG)
    {
        if (!m_recombined)
        {
            m_recombined = p_RNG.nextDouble() < 0.01;
        }
        
        return m_recombined;
    }
    
    public void resetRecombine()
    {
        m_recombined = false;
    }
    
    @Override
    public String toString()
    {
        return "(x = " + m_positionX + " ; y = " + m_positionY + " ; radius = " + m_radius;
    }
}
