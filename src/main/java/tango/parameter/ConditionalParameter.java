package tango.parameter;

import com.mongodb.BasicDBObject;
import com.mongodb.DBObject;
import ij.gui.GenericDialog;
import java.awt.Color;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;
import javax.swing.BorderFactory;
import javax.swing.Box;
import javax.swing.border.BevelBorder;
import tango.gui.util.CollapsiblePanel;
import tango.gui.util.Displayer;
import tango.gui.util.Refreshable;
import tango.util.Utils;

/**
 *
 **
 * /**
 * Copyright (C) 2012 Jean Ollion
 *
 *
 *
 * This file is part of tango
 *
 * tango is free software; you can redistribute it and/or modify it under the
 * terms of the GNU General Public License as published by the Free Software
 * Foundation; either version 3 of the License, or (at your option) any later
 * version.
 *
 * This program is distributed in the hope that it will be useful, but WITHOUT
 * ANY WARRANTY; without even the implied warranty of MERCHANTABILITY or FITNESS
 * FOR A PARTICULAR PURPOSE. See the GNU General Public License for more
 * details.
 *
 * You should have received a copy of the GNU General Public License along with
 * this program. If not, see <http://www.gnu.org/licenses/>.
 *
 * @author Jean Ollion
 */
public class ConditionalParameter extends Parameter implements Refreshable, NestedParameter {
    ActionnableParameter actionnableParameter;
    protected HashMap<Object, Parameter[]> parameters;
    Parameter[] currentParameters;
    Parameter[] defaultParameters;
    Box mainBox;
    
    public ConditionalParameter(String title, ActionnableParameter actionnableParameter, HashMap<Object, Parameter[]> parameters) {
        super(actionnableParameter.getParameter().getId());
        init(title, actionnableParameter);
        this.parameters=parameters;
        currentParameters=parameters.get(actionnableParameter.getValue());
        for (Map.Entry<Object, Parameter[]> e : parameters.entrySet()) {
            Parameter[] array = parameters.get(e.getKey());
            if (array!=null) {
                for (int i = 0; i<array.length; i++) {
                    array[i].setParent(this);
                    array[i].setCompulsary(compulsary);
                }
            }
        }
        updateCurretParameters();
    }
    
    public ConditionalParameter(String title, ActionnableParameter actionnableParameter) {
        super(actionnableParameter.getParameter().getId());
        init(title, actionnableParameter);
        parameters = new HashMap<Object, Parameter[]> ();
    }
    
    public ConditionalParameter(ActionnableParameter actionnableParameter) {
        super(actionnableParameter.getParameter().getId());
        init(null, actionnableParameter);
        parameters = new HashMap<Object, Parameter[]> ();
    }
    
    @Override
    public void toggleVisibility(boolean visible) {
        ((CollapsiblePanel)box).toggleVisibility(visible);
    }
    
    public void setDefaultParameter(Parameter[] parameters) {
        if (parameters==null) return;
        this.defaultParameters=parameters;
        for (Parameter p : parameters) {
            p.setParent(this);
            p.setCompulsary(compulsary);
        }
        updateCurretParameters();
    }
    
    public void setCondition(Object condition, Parameter[] parameters) {
        if (condition==null) return;
        this.parameters.put(condition, parameters);
        if (parameters!=null) {
            for (Parameter p : parameters) {
                p.setParent(this);
                p.setCompulsary(compulsary);
            }
        }
        updateCurretParameters();
    }
    
    protected void updateCurretParameters() {
        if (currentParameters!=null) for (Parameter p : currentParameters) p.removeFromContainer(mainBox);
        currentParameters=getCurrentParameters();
        if (currentParameters!=null) for (Parameter p : currentParameters) p.addToContainer(mainBox);
        setColor();
    }
    
    protected void init(String title, ActionnableParameter actionnableParameter) {
        this.label=actionnableParameter.getParameter().label;
        box= new CollapsiblePanel(title, label);
        this.actionnableParameter=actionnableParameter;
        actionnableParameter.setRefreshOnAction(this);
        mainBox = Box.createVerticalBox();
        //mainBox.setBorder(BorderFactory.createBevelBorder(BevelBorder.LOWERED));
        actionnableParameter.getParameter().addToContainer(mainBox);
        box.add(mainBox);
        actionnableParameter.getParameter().setParent(this);
        actionnableParameter.getParameter().setCompulsary(compulsary);
    }    
    @Override
    public void dbPut(DBObject dbo) {
        BasicDBObject subDBO = new BasicDBObject();
        actionnableParameter.getParameter().dbPut(subDBO);
        subDBO.append("isCollapsed", ((CollapsiblePanel)box).isCollapsed());
        if (currentParameters!=null) for (Parameter p : currentParameters) p.dbPut(subDBO);
        dbo.put(id, subDBO);
    }

    @Override
    public void dbGet(BasicDBObject dbo) {
        Object o = dbo.get(id);
        if (!(o instanceof BasicDBObject)) return;
        BasicDBObject subDBO=(BasicDBObject)dbo.get(id);
        if (subDBO!=null) {
            actionnableParameter.getParameter().dbGet(subDBO);
            toggleVisibility(!subDBO.getBoolean("isCollapsed", false));
        }
        if (currentParameters!=null) for (Parameter p : currentParameters) p.removeFromContainer(mainBox);
        currentParameters=getCurrentParameters();
        if (currentParameters!=null) for (Parameter p : currentParameters) {
            if (subDBO!=null) p.dbGet(subDBO);
            p.addToContainer(mainBox);
        }
    }
    
    @Override
    public void setContent(Parameter p) {
        if (p instanceof ConditionalParameter) {
            ConditionalParameter cp = (ConditionalParameter)p;
            this.actionnableParameter.getParameter().setContent(cp.getActionnableParameter());
            Parameter.setContent(currentParameters, cp.getParameters());
        } else if (p.getClass() == this.actionnableParameter.getClass()) {
            this.actionnableParameter.getParameter().setContent(p);
            
        }
    }
    
    @Override
    public Parameter duplicate(String newLabel, String newId) {
        HashMap<Object, Parameter[]>  newParameters = new HashMap<Object, Parameter[]>(parameters.size());
        for (Object key : parameters.keySet()) {
            newParameters.put(key, Parameter.duplicateArray(parameters.get(key)));
        }
        ConditionalParameter res=  new ConditionalParameter( newLabel ,(ActionnableParameter)actionnableParameter.getParameter().duplicate(actionnableParameter.getParameter().getLabel(), newId), newParameters);
        if (defaultParameters!=null) res.setDefaultParameter(Parameter.duplicateArray(defaultParameters));
        return res;
    }

    private Parameter[] getCurrentParameters() {
        Parameter[] res = parameters.get(actionnableParameter.getValue());
        if (res==null) return defaultParameters;
        else return res;
    }
    
    public Parameter[] getDefaultParameters() {
        return defaultParameters;
    }
    
    @Override
    public void refresh() {
        if (currentParameters!=null) {
            for (Parameter p : currentParameters) p.removeFromContainer(mainBox);
            if (ml!=null) unRegister();
        }
        this.currentParameters=getCurrentParameters();
        if (currentParameters!=null) {
            for (Parameter p : currentParameters) p.addToContainer(mainBox);
            if (ml!=null) register();
        }
        mainBox.revalidate();
        mainBox.repaint();
        //if (displayer!=null) displayer.refreshDisplay();
        box.repaint();
        //else System.out.println("no displayer !!");
        if (parameters==null) return;
        for (Parameter[] p : parameters.values()) if (p!=null) for (Parameter pp:p) if (pp instanceof Refreshable) ((Refreshable)pp).refresh();
        setColor();
    }

    @Override
    public void addToGenericDialog(GenericDialog gd) {
       // TODO add all parameters included i nthe condition ;
    }
    
    @Override 
    protected void register() {
        this.actionnableParameter.getParameter().register(ml);
        box.addMouseListener(this);
        if (currentParameters!=null) for (Parameter p : this.currentParameters) p.register(ml);
    }
    
    @Override 
    protected void unRegister() {
        this.actionnableParameter.getParameter().unRegister(ml);
        box.removeMouseListener(this);
        if (currentParameters!=null) for (Parameter p : this.currentParameters) p.unRegister(ml);
    }

    @Override
    public Parameter[] getParameters() {
        return currentParameters;
    }
    
    public Parameter getActionnableParameter() {
        return actionnableParameter.getParameter();
    }
    
    @Override
    public boolean sameContent(Parameter p) {
        if (p instanceof ConditionalParameter && ((ConditionalParameter)p).getActionnableParameter().sameContent(getActionnableParameter())) {
            return Parameter.sameValue(this.getParameters(), ((ConditionalParameter)p).getParameters());
        } else return false;
    }
    
    @Override
    public boolean isValid() {
        if (!this.compulsary) return true;
        if (this.getActionnableParameter().isValidOrNotCompulsary()) {
            Parameter[] array = getParameters();
            if (array==null) return true;
            for (Parameter p : array) if (!p.isValidOrNotCompulsary()) return false;
            return true;
        }
        else return false;
    }
    
    @Override 
    public void setTemplate(Parameter template) {
        super.setTemplate(template);
        if (template instanceof ConditionalParameter) {
            ConditionalParameter cpt = (ConditionalParameter)template;
            if (cpt.parameters!=null && parameters!=null) {
                for (Map.Entry<Object, Parameter[]> e : ((ConditionalParameter)template).parameters.entrySet()) {
                    Parameter[] array = parameters.get(e.getKey());
                    if (array!=null && array.length==e.getValue().length) {
                        for (int i = 0; i<array.length; i++) {
                            array[i].setTemplate(e.getValue()[i]);
                        }
                    }
                }
                if (defaultParameters!=null && cpt.getDefaultParameters()!=null && cpt.getDefaultParameters().length==defaultParameters.length) {
                    for (int i = 0; i<defaultParameters.length; i++) {
                        defaultParameters[i].setTemplate(cpt.getDefaultParameters()[i]);
                    }
                }
                getActionnableParameter().setTemplate(cpt.getActionnableParameter());
            }
        }
    }
    
    @Override
    public void setCompulsary(boolean compulsary) {
        super.setCompulsary(compulsary);
        if (defaultParameters!=null) for (Parameter p : defaultParameters) if (p!=null) p.setCompulsary(compulsary); 
        for (Parameter[] ps : this.parameters.values()) {
            if (ps!=null) for (Parameter p : ps) if (p!=null) p.setCompulsary(compulsary);
        }
    }
    
}
