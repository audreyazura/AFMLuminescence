/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package afmluminescence.guimanager;

import afmluminescence.luminescencegenerator.GeneratorManager;
import java.math.BigDecimal;
import afmluminescence.luminescencegenerator.ImageBuffer;

/**
 *
 * @author audreyazura
 */
public class Launcher
{

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args)
    {
        CanvasManager absorberRepresentation = new CanvasManager();
        ((CanvasManager) absorberRepresentation).startVisualizer();
    }
    
}
