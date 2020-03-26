package tileworld.exceptions;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */



/**
 * NotMovableException
 *
 * @author michaellees
 * Created: Apr 21, 2010
 *
 * Copyright michaellees 2010
 *
 *
 * Description:
 *
 * Thrown when move is called on a TWEntity which is not movable (e.g., Hole)
 */
class NotMovableException extends Exception{

    public NotMovableException(String string) {
    }

}
