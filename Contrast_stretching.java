import ij.*;
import ij.process.*;
import ij.gui.*;
import ij.text.*;
import java.awt.*;
import java.awt.geom.*;
import java.awt.event.*;
import ij.plugin.frame.*;
import java.util.*;
import javax.swing.JTextField;

/**
*This plugin implements the contrast stretching operator
 *
*@author Bosco Camillo-BS degree in Computer Science Advisor: Prof. Battiato
*Sebastiano Organization: University of Catania-ITALY
*
*Updated by Manuel Guillermo Forero,Cristian Morera, NAtalia Hernández and Mariano G. Montero C.
 */
public class Contrast_stretching extends PlugInFrame implements ActionListener
{
    private Font monoFont=new Font("Monospaced",Font.PLAIN,12); //Font for GUI
    private Font sans=new Font("SansSerif",Font.BOLD,12); //Font for GUI
    private ValueViewerCS viewer; 
    private CSPlot plot; //
    private Button preview; //Button to activate the image preview
    private Button apply; //Button to apply the changes to the image
    private Button Transfer; //Button to show a menu with Transference Methods
    /** Variables para el manejo de métodos  
     *  de transferencia por medio de interfaz gráfica en el panel principal.
    */
    private Label TitleTransfers, TitleParam, TitleGrafics;
    private JTextField ParamTransfers;
    private Choice chTransferenceMethods;
    private Button btnApplayMethod;
    private Checkbox bGraphic2;
    //----------------------------------------------
    private Button controlPointsButton;//Button to set the number of small circles to display in the graphic
    private ImageProcessor ip;
    private ImagePlus imp;
    private int type; //Variable to check the type of the image
    double n=1; 
    boolean bGraphic=true; //Variable to check if a graphic is shown or not
    int method; //Variable to check which methods is applied to the image
    int nControlPoints=3; //Number of small circles to display in the graphic
    int[] pointsX; //Varible
    int[] pointsY;
    private Object ventana;
    //Constructor of the class
    public Contrast_stretching() 
    {
        super("Contrast Stretching Transform");
        setResizable(false);
    }

    //Method that manage the logic of this plugin and what is running
    @Override
    public void run(String arg)
    {
        //System.out.println(System.getProperty("java.version").substring(0, 3));
        //Varible to get the current image
        imp=WindowManager.getCurrentImage();
        //Check if the image is null
        if (imp==null)
        {
            IJ.showStatus("No image");
            IJ.noImage();
            return;
        }
        ip=imp.getProcessor(); //Get the processor to edit the image values
        type=imp.getType(); //Get the type of the current image
        //Check if the image is of the correct type
        if ((type==ImagePlus.GRAY16)||(type==ImagePlus.GRAY32))
        {
            IJ.error("Contrast Stretching requires an image of type\n \n8-bit grayscale\n8-bit indexed color\nRGB color");
            return;
        }
        ventana = this;
        //Layout to accommodate the GUI components 
        GridBagLayout gridbag=new GridBagLayout();
        GridBagConstraints c=new GridBagConstraints();
        //Set the Layout to the PlugInFrame
        setLayout(gridbag);
        //PanelY
        Panel panelY=new Panel();
        c.gridx=0;
        c.gridy=1;
        c.insets=new Insets(0,10,0,0);
        gridbag.setConstraints(panelY,c);
        panelY.setLayout(new GridLayout(8,1));
        Panel pan=new Panel();
        Label maxLabelY=new Label("255",Label.RIGHT);
        maxLabelY.setFont(monoFont);
        pan.add(maxLabelY);
        panelY.add(pan);
        add(panelY);
        //Panel title
        Panel paneltitle=new Panel();
        c.gridx=1;
        int y=0;
        c.gridy=y++;
        c.fill=GridBagConstraints.BOTH;
        c.anchor=GridBagConstraints.CENTER;
        c.insets=new Insets(5,10,0,10);//top,left,bottom,right
        gridbag.setConstraints(paneltitle,c);
        // Plot
        plot=new CSPlot();
        c.gridy=y++;
        c.insets=new Insets(10,10,0,10);
        gridbag.setConstraints(plot,c);
        add(plot);
        // panellab
        Panel panellab=new Panel();
        c.gridy=y++;
        c.insets=new Insets(0,2,0,6);
        gridbag.setConstraints(panellab,c);
        panellab.setLayout(new BorderLayout());
        Label minLabel=new Label("0",Label.LEFT);
        minLabel.setFont(monoFont);
        panellab.add("West",minLabel);
        Label maxLabel=new Label("255",Label.RIGHT);
        maxLabel.setFont(monoFont);
        panellab.add("East",maxLabel);
        add(panellab);

        // panel List
        Panel panelList=new Panel();
        c.gridy=y++;
        c.insets=new Insets(0,10,0,0);
        gridbag.setConstraints(panelList,c);
        Panel global=new Panel();
        Label mouseLabel=new Label("(--- ,---)",Label.LEFT);
        mouseLabel.setFont(monoFont);
        global.add(mouseLabel);
        Panel panelButtonList=new Panel();
        Button list=new Button("List");
        viewer=new ValueViewerCS(plot.getLUT());
        list.addActionListener(viewer);
        plot.setViewer(viewer);
        GridBagLayout gridbag_list=new GridBagLayout();
        GridBagConstraints c_list=new GridBagConstraints();
        panelButtonList.setLayout(gridbag_list);
        c_list.ipadx=10;
        c_list.ipady=10;
        gridbag_list.setConstraints(panelButtonList,c_list);
        panelButtonList.add(list);
        global.add(panelButtonList);
        panelList.add(global);
        add(panelList);
        plot.setLabelMouse(mouseLabel);
        //Panel para botones de Transferencia de Tipo Potencia
        Panel panelTittle2 = new Panel();
        TitleGrafics = new Label("Buttons for grafic");
        TitleGrafics.setFont(sans);
        panelTittle2.add(TitleGrafics);
        //--------------------------------------
        //Panel para botones de Transferencia de Tipo Potencia
        Panel panelTittle = new Panel();
        TitleTransfers = new Label("Transfer Functions");
        TitleTransfers.setFont(sans);
        panelTittle.add(TitleTransfers);
        //--------------------------------------
        // panel Preview
        Panel panelPreview=new Panel();
        preview=new Button("Preview");
        preview.addActionListener(this);
        panelPreview.add(preview);
        //panel Transfer_function
        Panel panelTransfer=new Panel();
        Transfer=new Button("Transfer");
        Transfer.addActionListener(this);
        panelTransfer.add(Transfer);
        // panel Apply
        Panel panelApply=new Panel();
        apply=new Button("Apply Chart");
        apply.addActionListener(this);
        panelApply.add(apply);
        //----------------------------------------------------------------------
        Panel panelMethdsP4 = new Panel();
        chTransferenceMethods = new Choice();
        chTransferenceMethods.add("Power");
        chTransferenceMethods.add("Root");
        chTransferenceMethods.add("Natural Logarithm");
        chTransferenceMethods.add("Sigmoid Sin");
        chTransferenceMethods.add("Sigmoid Tan");
        chTransferenceMethods.add("Sigmoid N.N.");
        chTransferenceMethods.addItemListener(
            new ItemListener() {
                @Override
                public void itemStateChanged(ItemEvent ie) {
                    String choice = chTransferenceMethods.getSelectedItem();
                    byte[] pixeles=(byte[])ip.getPixels();
                    int size=ip.getHeight()*ip.getWidth();
                    double[] fx=new double[256];
                    double[] fy=new double[256];
                    int nPoints=0;                  // contador      
                    int nControlPoints2 = 0;
                    int[] controlPoints=new int[nControlPoints]; // vector de puntos
                    double p = Double.parseDouble(ParamTransfers.getText());
                    n=p;
                    if(choice.equals("Power"))
                    {
                        ParamTransfers.setText("2.0");
                        p = Double.parseDouble(ParamTransfers.getText());
                        if(p!=0)
                        {
                            //n=1./n;
                            for (int i=0;i<size;i++)//Potencia n de la imagen
                                pixeles[i]=(byte)(Math.pow(pixeles[i]&0xFF,n)/Math.pow(255.,n-1.));//(q^n)/((L-1)^(n-1))
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(Math.pow(i,n)/Math.pow(255,n-1));//i^1/n/(L-1)^1/(n-1)
                                    if(nPoints==(int)(256.0/nControlPoints)&nControlPoints2<nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints2++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.error("n must be different of 0");
                    }
                    else if(choice.equals("Root")){
                        ParamTransfers.setText("2.0");
                        p = Double.parseDouble(ParamTransfers.getText());
                        if(p!=0)
                        {
                            n=1./n;
                            for (int i=0;i<size;i++)//Raiz n de la imagen
                                pixeles[i]=(byte)(Math.pow(pixeles[i]&0xFF,n)/Math.pow(255.,n-1.));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(Math.pow(i,n)/Math.pow(255,n-1));//i^1/n/(L-1)^1/(n-1)
                                    if(nPoints==(int)(256.0/nControlPoints)&nControlPoints2<nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints2++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.error("n must be different of 0");
                    }
                    else if(choice.equals("Natural Logarithm"))
                    {
                        ParamTransfers.setText("0.9");
                        p = Double.parseDouble(ParamTransfers.getText());
                        if(p!=0)
                        {
                            n=1./n;
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(255.*(Math.log((pixeles[i]&0xFF)+1.)/Math.log(256.)));//(q(x,y)^n)/(255^(n-1))
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(255.*(Math.log(1+i)/Math.log(256.)));
                                    if(nPoints==(int)(256.0/nControlPoints)&nControlPoints2<nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints2++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.error("n must be different of 0");
                    }
                    else if(choice.equals("Sigmoid Sin"))
                    {
                        ParamTransfers.setText(("0.9"));
                        p = Double.parseDouble(ParamTransfers.getText());
                        n = p;
                        if(n<=1)
                        { 
                            double alpha_pi=n*Math.PI;
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(127.5*(1.+(1./Math.sin(alpha_pi/2.)*Math.sin(alpha_pi*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(127.5*(1+(1/Math.sin(alpha_pi/2)*Math.sin(alpha_pi*(((double)i/255.)-0.5)))));
                                    if(nPoints==(int)(256.0/nControlPoints)&nControlPoints2<nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints2++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.showMessage("n must be between 0 and 1");
                    }
                    else if(choice.equals("Sigmoid Tan")){
                        ParamTransfers.setText(("0.9"));
                        p = Double.parseDouble(ParamTransfers.getText());
                        n = p;
                        if(n<=1)
                        {
                            double alfa_pi=n*Math.PI;
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(127.5*(1.+(1./Math.tan(alfa_pi/2.)*Math.tan(alfa_pi*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++) //Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(127.5*(1+(1/Math.tan(alfa_pi/2)*Math.tan(alfa_pi*(((double)i/255.)-0.5)))));
                                    if(nPoints==(int)(256.0/nControlPoints)&nControlPoints2<nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints2++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.showMessage("n must be between 0 and 1");
                    }
                    else if(choice.equals("Sigmoid N.N.")){
                        ParamTransfers.setText(("0.9"));
                        p = Double.parseDouble(ParamTransfers.getText());
                        n = p;
                        if(n<=1)
                        {
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(255.*(1./(1./Math.exp(-2.*Math.log(254.*n)*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++) //Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(255.*(1./(1.+Math.exp(-2.*Math.log(254.*n)*(((double)i/255.)-0.5)))));
                                    if(nPoints==(int)(256.0/nControlPoints)&nControlPoints2<nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints2++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.showMessage("n must be between 0 and 1");
                    }
                    if(bGraphic2.getState()){
                        PlotWindow.noGridLines=false;
                        Plot plot1=new Plot("Transfer function","Input","Output",fx,fy);
                        plot1.setLimits(0,255,0,255);
                        plot1.setSize(480,480);
                        plot1.setLineWidth(2);
                        plot1.setColor(Color.blue);
                        plot1.show();
                    }
                }
            }
        );
        panelMethdsP4.add(chTransferenceMethods);
        btnApplayMethod = new Button("Apply Tranfer Function");
        btnApplayMethod.addActionListener(this);
        panelMethdsP4.add(btnApplayMethod);
        //Panel Number of control points
        Panel panelControlPoints=new Panel();
        controlPointsButton=new Button("Control points number");
        controlPointsButton.addActionListener(this);
        panelControlPoints.add(controlPointsButton);
        //----------------------------------------------------------------------
        Panel panelMethdsP3 = new Panel();
        bGraphic2 = new Checkbox("Draw Plot TF");
        bGraphic2.setState(false);
        panelMethdsP3.add(bGraphic2);
        Panel panelInputl = new Panel();
        TitleParam = new Label("Value:");
        TitleParam.setFont(sans);
        panelInputl.add(TitleParam);
        ParamTransfers = new JTextField();
        ParamTransfers.setToolTipText("Put a number for try a Tranference Method");
        ParamTransfers.setText("2.0");
        panelInputl.add(ParamTransfers);
        //---------------------------------------------------------------------
        // panel container
        Panel container=new Panel();
        container.setLayout(new GridLayout(5,1));
        Panel aux = new Panel();
        c.gridx=2;
        c.gridy=1;
        c.insets=new Insets(10,0,0,10);
        gridbag.setConstraints(container,c);
        container.add(panelTittle2);
        container.add(panelTittle);
        container.add(panelPreview);
        container.add(panelTransfer);
        container.add(panelApply);
        container.add(panelMethdsP4);
        container.add(panelControlPoints);
        container.add(panelInputl);
        container.add(aux);
        container.add(panelMethdsP3);
        add(container);
        //------------------
        pack();
        GUI.center(this);
        show();
    }
    @Override
    public void actionPerformed(ActionEvent e) 
    {
        Object source=e.getSource();
        if(source == btnApplayMethod)
        {
            try {
                byte[] pixeles=(byte[])ip.getPixels();
                int size=ip.getHeight()*ip.getWidth();
                double[] fx=new double[256];
                double[] fy=new double[256];
                int nPoints=0;                  // contador      
                int[] controlPoints=new int[nControlPoints]; // vector de puntos
                int nControlPoints=0;           // contador
                double p = Double.parseDouble(ParamTransfers.getText());
                n=p;
                int method = chTransferenceMethods.getSelectedIndex();
                switch(method)
                {
                    case 0:
                        if(p!=0)
                        {
                            //n=1./n;
                            for (int i=0;i<size;i++)//Raiz n de la imagen
                                pixeles[i]=(byte)(Math.pow(pixeles[i]&0xFF,n)/Math.pow(255.,n-1.));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(Math.pow(i,n)/Math.pow(255,n-1));//i^1/n/(L-1)^1/(n-1)
                                    if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.error("n must be different of 0");
                        break;
                    case 1:
                        if(p!=0)
                        {
                            n=1./n;
                            for (int i=0;i<size;i++)//Raiz n de la imagen
                                pixeles[i]=(byte)(Math.pow(pixeles[i]&0xFF,n)/Math.pow(255.,n-1.));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(Math.pow(i,n)/Math.pow(255,n-1));//i^1/n/(L-1)^1/(n-1)
                                    if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.error("n must be different of 0");
                        break;
                    case 2:
                        if(p!=0)
                        {
                            n=1./n;
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(255.*(Math.log((pixeles[i]&0xFF)+1.)/Math.log(256.)));//(q(x,y)^n)/(255^(n-1))
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(255.*(Math.log(1+i)/Math.log(256.)));
                                    if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.error("n must be different of 0");
                        break;
                    case 3:
                        if(n<=1)
                        { 
                            double alpha_pi=n*Math.PI;
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(127.5*(1.+(1./Math.sin(alpha_pi/2.)*Math.sin(alpha_pi*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++)//Funcion de tranferencia
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(127.5*(1+(1/Math.sin(alpha_pi/2)*Math.sin(alpha_pi*(((double)i/255.)-0.5)))));
                                    if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.showMessage("n must be between 0 and 1");
                        break;
                    case 4:
                        if(n<=1)
                        {
                            double alfa_pi=n*Math.PI;
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(127.5*(1.+(1./Math.tan(alfa_pi/2.)*Math.tan(alfa_pi*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++) //Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(127.5*(1+(1/Math.tan(alfa_pi/2)*Math.tan(alfa_pi*(((double)i/255.)-0.5)))));
                                    if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.showMessage("n must be between 0 and 1");
                        break;
                    case 5:
                        if(n<=1)
                        {
                            for (int i=0;i<size;i++)
                                pixeles[i]=(byte)(255.*(1./(1./Math.exp(-2.*Math.log(254.*n)*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                            if (bGraphic)
                            {
                                for (int i=0;i<256;i++) //Funcion de tranferencia.
                                {
                                    fx[i]=i;
                                    fy[i]=(double)(255.*(1./(1.+Math.exp(-2.*Math.log(254.*n)*(((double)i/255.)-0.5)))));
                                    if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints)//256/6=42,6 y 42*6=252
                                    {
                                        controlPoints[nControlPoints++]=(int)fy[i];
                                        nPoints=0;
                                    }
                                    nPoints++;
                                }
                                plot.CSPlotP(controlPoints);
                                plot.findLUT();
                            }
                        }
                        else
                            IJ.showMessage("n must be between 0 and 1");
                        break;
                }
                bGraphic = true;
                if(bGraphic2.getState())
                {
                    PlotWindow.noGridLines=false;
                    Plot plot1=new Plot("Transfer function","Input","Output",fx,fy);
                    plot1.setLimits(0,255,0,255);
                    plot1.setSize(480,480);
                    plot1.setLineWidth(2);
                    plot1.setColor(Color.blue);
                    plot1.show();
                    ((Frame)ventana).dispose();
                }
            } 
            catch (NumberFormatException nfe) 
            {
                 IJ.error(nfe.getMessage());
            }
        }
        if(source==controlPointsButton)
        {
            GenericDialog gd=new GenericDialog("Control points");
            gd.addNumericField("Control points number:",nControlPoints,0);
            gd.showDialog();
            int numPoints = (int)gd.getNextNumber();
            if(numPoints>=3&&numPoints<=20){
                nControlPoints=numPoints;
                plot.CSPlotI();
            }
        }
        if (source==preview) 
        {
            CSTransform cst=new CSTransform(imp,plot.getLUT(),imp.getTitle());
            ImagePlus processed=null;
            switch (type) 
            {
                case ImagePlus.GRAY8:
                    processed=cst.createProcessed8bitImage();
                    break;
                case ImagePlus.COLOR_256:
                    processed=cst.createProcessed8bitImage();
                    break;
                case ImagePlus.COLOR_RGB:
                    processed=cst.createProcessedRGBImage();
                    break;
                default:
                    break;
            }
            ImageCanvas canvas;
            ImagePlus im;
            im=processed;
            canvas=new ImageCanvas(im);
            ImageWindow imgWin=new ImageWindow(im);
            imgWin.setResizable(false);
            imgWin.setSize(canvas.getSize().width+150,canvas.getSize().height+150);
            GridBagLayout gridbag=new GridBagLayout();
            GridBagConstraints c=new GridBagConstraints();
            imgWin.setLayout(gridbag);
            //Panel Canvas
            c.gridx=0;
            int y=0;
            c.gridy=y++;
            c.insets=new Insets(10,10,0,10);
            gridbag.setConstraints(canvas,c);
            imgWin.add(canvas);
            //Panel Time
            Panel panelTime=new Panel();
            c.gridy=y++;
            c.insets=new Insets(2,10,0,10);
            gridbag.setConstraints(panelTime,c);
            imgWin.add(panelTime);
            Panel panelHisto=new Panel();
            c.gridy=y++;
            c.insets=new Insets(2,10,0,10);
            gridbag.setConstraints(panelHisto,c);
            imgWin.add(panelHisto);
            imgWin.show();
        }
        if (source==apply) 
        {
            Undo.setup(Undo.COMPOUND_FILTER,imp);
            CSTransform cst=new CSTransform(imp,plot.getLUT(),imp.getTitle());
            switch (type)
            {
                case ImagePlus.GRAY8:
                    cst.createProcessed8bitImage(imp);
                    break;
                case ImagePlus.COLOR_256:
                    cst.createProcessed8bitImage(imp);
                    break;
                case ImagePlus.COLOR_RGB:
                    cst.createProcessedRGBImage(imp);
                    break;
                default:
                    break;
            }
            imp.updateAndRepaintWindow();
            imp.changes=true;
            Undo.setup(Undo.COMPOUND_FILTER_DONE,imp);
        }
        if (source==Transfer)
        {
            String[] methods=new String[6];
            methods[0]="Power";
            methods[1]="Root";
            methods[2]="Natural logarithm";
            methods[3]="Sigmoid_(sin)";
            methods[4]="Sigmoid_(tan)";
            methods[5]="Sigmoid_(N.N.)";

            GenericDialog menu=new GenericDialog("Transfer function");
            menu.addNumericField("Value",n,1);
            menu.addChoice("Function",methods,methods[0]);
            menu.addCheckbox("Plot TF",bGraphic);
            menu.showDialog();
            if (menu.wasCanceled())
                return;
            n=menu.getNextNumber();
            method=(int)menu.getNextChoiceIndex();
            bGraphic=(boolean)menu.getNextBoolean();
            byte[] pixeles=(byte[])ip.getPixels();
            int size=ip.getHeight()*ip.getWidth();
            double[] fx=new double[256];
            double[] fy=new double[256];
            int nPoints=0;                  // contador      
            int[] controlPoints=new int[nControlPoints]; // vector de puntos
            int nControlPoints=0;           // contador
            switch(method)
            {
                case 0://Power
                    if(n!=0)
                    {
                        //n=1./n;
                        for (int i=0;i<size;i++)//Potencia n de la imagen
                            pixeles[i]=(byte)(Math.pow(pixeles[i]&0xFF,n)/Math.pow(255.,n-1.));//(q^n)/((L-1)^(n-1))
                        if (bGraphic)
                        {
                            for (int i=0;i<256;i++)//Funcion de tranferencia
                            {
                                fx[i]=i;
                                fy[i]=(double)(Math.pow(i,n)/Math.pow(255.,n-1.));//q^n/(L-1)^n-1
                                if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints+1)//256/6=42,6 y 42*6=252
                                {
                                    controlPoints[nControlPoints++]=(int)fy[i];
                                    nPoints=0;
                                }
                                nPoints++;
                            }
                            plot.CSPlotP(controlPoints);
                            plot.findLUT();
                        }
                    }
                    else
                        IJ.error("n must be different of 0");
                    break;
                case 1://raiz
                    if(n!=0)
                    {
                        n=1./n;
                        for (int i=0;i<size;i++)//Raiz n de la imagen
                            pixeles[i]=(byte)(Math.pow(pixeles[i]&0xFF,n)/Math.pow(255.,n-1.));
                        if (bGraphic)
                        {
                            for (int i=0;i<256;i++)//Funcion de tranferencia.
                            {
                                fx[i]=i;
                                fy[i]=(double)(Math.pow(i,n)/Math.pow(255,n-1));//i^1/n/(L-1)^1/(n-1)
                                if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints+1)//256/6=42,6 y 42*6=252
                                {
                                    controlPoints[nControlPoints++]=(int)fy[i];
                                    nPoints=0;
                                }
                                nPoints++;
                            }
                            plot.CSPlotP(controlPoints);
                            plot.findLUT();
                        }
                    }
                    else
                    IJ.showMessage("n must be different of 0");
                        break;
                case 2://Logaritmo natural
                    if(n!=0)
                    {
                        n=1./n;
                        for (int i=0;i<size;i++)
                            pixeles[i]=(byte)(255.*(Math.log((pixeles[i]&0xFF)+1.)/Math.log(256.)));//(q(x,y)^n)/(255^(n-1))
                        if (bGraphic)
                        {
                            for (int i=0;i<256;i++)//Funcion de tranferencia.
                            {
                                fx[i]=i;
                                fy[i]=(double)(255.*(Math.log(1+i)/Math.log(256.)));
                                if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints+1)//256/6=42,6 y 42*6=252
                                {
                                    controlPoints[nControlPoints++]=(int)fy[i];
                                    nPoints=0;
                                }
                                nPoints++;
                            }
                            plot.CSPlotP(controlPoints);
                            plot.findLUT();
                        }
                    }
                    break;
                case 3://Sigmoid calculated by using the sin function 
                    if(n<=1)
                    { 
                        double alpha_pi=n*Math.PI;
                        for (int i=0;i<size;i++)
                            pixeles[i]=(byte)(127.5*(1.+(1./Math.sin(alpha_pi/2.)*Math.sin(alpha_pi*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                        if (bGraphic)
                        {
                            for (int i=0;i<256;i++)//Funcion de tranferencia
                            {
                                fx[i]=i;
                                fy[i]=(double)(127.5*(1+(1/Math.sin(alpha_pi/2)*Math.sin(alpha_pi*(((double)i/255.)-0.5)))));
                                if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints+1)//256/6=42,6 y 42*6=252
                                {
                                    controlPoints[nControlPoints++]=(int)fy[i];
                                    nPoints=0;
                                }
                                nPoints++;
                            }
                            plot.CSPlotP(controlPoints);
                            plot.findLUT();
                        }
                    }
                    else
                        IJ.showMessage("n must be between 0 and 1");
                    break;
                case 4://Sigmoid calculated by using the tan function
                    if(n<=1)
                    {
                        double alfa_pi=n*Math.PI;
                        for (int i=0;i<size;i++)
                            pixeles[i]=(byte)(127.5*(1.+(1./Math.tan(alfa_pi/2.)*Math.tan(alfa_pi*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                        if (bGraphic)
                        {
                            for (int i=0;i<256;i++) //Funcion de tranferencia.
                            {
                                fx[i]=i;
                                fy[i]=(double)(127.5*(1+(1/Math.tan(alfa_pi/2)*Math.tan(alfa_pi*(((double)i/255.)-0.5)))));
                                if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints+1)//256/6=42,6 y 42*6=252
                                {
                                    controlPoints[nControlPoints++]=(int)fy[i];
                                    nPoints=0;
                                }
                                nPoints++;
                            }
                            plot.CSPlotP(controlPoints);
                            plot.findLUT();
                        }
                    }
                    else
                        IJ.showMessage("n must be between 0 and 1");
                    break;
                case 5://Sigmoid by using the neural network function
                    if(n<=1)
                    {
                        for (int i=0;i<size;i++)
                            pixeles[i]=(byte)(255.*(1./(1./Math.exp(-2.*Math.log(254.*n)*(((double)(pixeles[i]&0xFF)/255.)-0.5)))));
                        if (bGraphic)
                        {
                            for (int i=0;i<256;i++) //Funcion de tranferencia.
                            {
                                fx[i]=i;
                                fy[i]=(double)(255.*(1./(1.+Math.exp(-2.*Math.log(254.*n)*(((double)i/255.)-0.5)))));
                                if(nPoints==(int)(256.0/this.nControlPoints)&nControlPoints<this.nControlPoints+1)//256/6=42,6 y 42*6=252
                                {
                                    controlPoints[nControlPoints++]=(int)fy[i];
                                    nPoints=0;
                                }
                                nPoints++;
                            }
                            plot.CSPlotP(controlPoints);
                            plot.findLUT();
                        }
                    }
                    else
                        IJ.showMessage("n must be between 0 and 1");
                    break;
            }
            if(bGraphic==true)
            {
                PlotWindow.noGridLines=false;
                Plot plot1=new Plot("Transfer function","Input","Output",fx,fy);
                plot1.setLimits(0,255,0,255);
                plot1.setSize(480,480);
                plot1.setLineWidth(2);
                plot1.setColor(Color.blue);
                plot1.show();
                ((Frame)ventana).dispose();
            }
            imp.updateAndDraw();
        }
    }

class ValueViewerCS implements ActionListener 
{
    private int[] lut;

    public ValueViewerCS(int[] l) 
    {
        lut=l;
    }

    public void setLUT(int[] l) 
    {
        System.arraycopy(l,0,lut,0,256);
    }

    @Override
    public void actionPerformed(ActionEvent e) 
    {
        StringBuilder sb=new StringBuilder();
        String headings="X\tY";
        for (int i=0;i<lut.length;i++) 
        { sb.append(IJ.d2s(i,0)).append("\t").append(IJ.d2s(lut[i],0)).append("\n");}
        TextWindow tw=new TextWindow("Plot Values",headings,sb.toString(),200,400);
        tw.show();
    }
}

final class CSPlot extends Canvas implements MouseMotionListener,MouseListener 
{
    private Contrast_stretching c;
    private static final int WIDTH=255,HEIGHT=255;
    private Label labMouse;
    private int[] y_values=new int[256];
    private int[] in;
    private int[] out;
    private Ellipse2D[] arrayEllisses;
    private boolean[] arrayCircles;
    private boolean[] arrayBools;
    private ValueViewerCS view;

    /* linea recta */
    public CSPlot()
    {
        c=new Contrast_stretching();
        arrayEllisses = new Ellipse2D[nControlPoints];
        arrayCircles = new boolean[nControlPoints];
        arrayBools = new boolean[nControlPoints];
        //puntos iniciales de funcion de transferencia manipulables por el usuario 
        in=new int[nControlPoints];
        out=new int[nControlPoints];   
        float intervaloInicial=255/4;
        int interval=0;
        for(int i=0;i<nControlPoints; i++,interval+=intervaloInicial)
            in[i]=out[i]=interval;
        in[nControlPoints-1]=out[nControlPoints-1]=255;
        int j=0;
        for(int i=0;i<nControlPoints; i++)
        {
            if(i!=nControlPoints-1)
                in[i]=out[i]=j;
            else
                in[i]=out[i]=255;
            j+=(int)(255.0/(double)nControlPoints);
        }
        addMouseMotionListener(this);
        addMouseListener(this);
        setSize(WIDTH,HEIGHT);
        findLUT();
    }
    public void CSPlotI(){
        c=new Contrast_stretching();
        arrayEllisses = new Ellipse2D[nControlPoints];
        arrayCircles = new boolean[nControlPoints];
        arrayBools = new boolean[nControlPoints];
        //puntos iniciales de funcion de transferencia manipulables por el usuario 
        in=new int[nControlPoints];
        out=new int[nControlPoints];   
        float intervaloInicial=255/4;
        int interval=0;
        for(int i=0;i<nControlPoints; i++,interval+=intervaloInicial)
            in[i]=out[i]=interval;
        in[nControlPoints-1]=out[nControlPoints-1]=255;
        int j=0;
        for(int i=0;i<nControlPoints; i++)
        {
            
            if(i!=nControlPoints-1)
                in[i]=out[i]=j;
            else
                in[i]=out[i]=255;
            int aux = (int)(255.0/(double)nControlPoints+4);
            j+=(j+aux>255)?255:aux;
        }
        setSize(WIDTH,HEIGHT);
        findLUT();
    }
    public void CSPlotP(int[] controlPoints)
    {
        c=new Contrast_stretching();
        int j=0;
        System.out.println(controlPoints.length+"-"+nControlPoints);
        for(int i=0;i<nControlPoints; i++)
        {
            if(i!=nControlPoints-1)
                in[i]=out[i]=j;
            else
                in[i]=255;
            int aux = (int)(255.0/(double)nControlPoints+1);
            j+=(j+aux>255)?255:aux;
        }
        for (int i = 0; i < controlPoints.length; i++) {
            if(i!=0)
                out[i]=controlPoints[i];
            else
                out[0]=0;
        }
    }

    public void setViewer(ValueViewerCS v) 
    {
        view=v;
    }

    public int[] getLUT() 
    {
        return y_values;
    }

    public void setLabelMouse(Label l) 
    {
        labMouse=l;
    }

    @Override
    public Dimension getPreferredSize() 
    {
        return new Dimension(WIDTH+1,HEIGHT+1);
    }

    @Override
    public void update(Graphics g) 
    {
        paint(g);
    }

    @Override
    public void paint(Graphics g) 
    {
        g.setColor(Color.white);
        g.fillRect(0,0,WIDTH,HEIGHT);
        g.setColor(Color.black);
        g.drawRect(0,0,WIDTH,HEIGHT);
        g.setColor(Color.red);
        Vector v=new Vector();
        for (int i=0;i<255;i++) 
        {
            int[] first_couple=coordsConverter(i,y_values[i]);
            int[] second_couple=coordsConverter((i+1),y_values[i+1]);
            g.drawLine(first_couple[0],first_couple[1],first_couple[0],first_couple[1]);
            g.drawLine(second_couple[0],second_couple[1],second_couple[0],second_couple[1]);
            approximation(g,second_couple[0],second_couple[1],first_couple[0],first_couple[1],v);
        }
        arrayEllisses = new Ellipse2D[nControlPoints];
        for (int i = 0; i < nControlPoints; i++) {
            int[] couple;
            if(i==0)
                couple =coordsConverter(0, out[i]);
            else if(i==nControlPoints-1)
                couple = coordsConverter(255, out[i]);
            else
                couple = coordsConverter(in[i], out[i]);
            Ellipse2D e = new Ellipse2D.Double(couple[0]-5,couple[1]-5,13,13);
            arrayEllisses[i] = e;
        }
        Graphics2D g2=(Graphics2D)g;
        g2.setColor(Color.green);
        for(int i = 0; i < arrayEllisses.length; i++)
            g2.fill(arrayEllisses[i]);
    }

    public void approximation(Graphics g,int succ_x,int succ_y,int prev_x,int prev_y,Vector v) 
    {
        g.drawLine(succ_x,succ_y,prev_x,prev_y);
    }

    private int[] coordsConverter(int x,int y) 
    {
        int[] converted_coord=new int[2];
        converted_coord[0]=x;
        converted_coord[1]=255-y;
        return converted_coord;
    }

    public void findLUT() 
    {
        int[] y_axis=new int[256];
        boolean flag=true;
        for (int i = 0; i < in.length; i++) 
        {
            if(in[i]!=out[i]){
                flag=false;
                break;
            }
        }
        if(flag)
            for (int i=0;i<256;i++) 
                y_axis[i]=i;//dibuja linea recta
        else 
        {
            for (int j = 0; j <= in[0]; j++)
                y_axis[j]=findYCoordRect(j,0,0,0,0);
            for (int i = 0; i < in.length-1; i++) 
            {
                for (int j = in[i]; j <= in[i+1]; j++)
                    y_axis[j]=findYCoordRect(j,in[i],out[i],in[i+1],out[i+1]);
            }
            for (int j = in[in.length-1]; j <= 255; j++)
                        y_axis[j]=findYCoordRect(j,in[in.length-1],out[in.length-1],255,255);
        }
        System.arraycopy(y_axis,0,y_values,0,256);
        if (view!=null)
            view.setLUT(y_axis);
        repaint();
    }

    public int findYCoordRect(int x,int x0,int y0,int x1,int y1) 
    {
        int y;
        if (y0==y1)
            y=y1;
        else if (x0==x1)
            y=y1;
        else 
            y=(int)((((float) (x-x0)/(x1-x0))*(y1-y0))+y0);
        return y;
    }

    @Override
    public void mouseMoved(MouseEvent e) 
    {
        arrayCircles = new boolean[nControlPoints];
        boolean flag = false;
        for (int i = 0; i < arrayEllisses.length; i++) {
            if(arrayEllisses[i] == null){
                flag = true;   
                break;
            }
        }
        if(!flag)
            arrayCircles[0] = arrayEllisses[0].contains(e.getX(),e.getY());
        arrayBools = new boolean[nControlPoints];
        for (int i = 0; i < arrayEllisses.length; i++) {
            arrayBools[i] = arrayEllisses[i].contains(e.getX(),e.getY());
        }
        int[]coord=coordsConverter(e.getX(),e.getY());
        labMouse.setText("("+coord[0]+" ,"+coord[1]+")");
    }

    @Override
    public void mouseDragged(MouseEvent e) 
    {
        int[] coord=coordsConverter(e.getX(),e.getY());
        labMouse.setText("("+coord[0]+" ,"+coord[1]+")");
        if(arrayBools[0]&&coord[0]>=0&&coord[0]<=in[1]&&coord[1]>=0&&coord[1]<=255)
            out[0]=coord[1];
        if(arrayBools[1]&&coord[0]>0&&coord[0]<=in[2]&&coord[1]>=0&&coord[1]<=255)
        {
            in[1]=coord[0];
            out[1]=coord[1];
        }
        for (int i = 2; i < arrayBools.length-1; i++) 
        {
            if(arrayBools[i]&&coord[0]>=in[i-1]&&coord[0]<=in[i+1]&&coord[1]>=0&&coord[1]<=255)
            {
                in[i]=coord[0];
                out[i]=coord[1];
            }
        }
        if(arrayBools[arrayBools.length-1]&&coord[0]>=in[in.length-2]&&coord[0]<=255&&coord[1]>=0&&coord[1]<=255)
            out[out.length-1]=coord[1];
        findLUT();
    }

    @Override
    public void mousePressed(MouseEvent e) {}
    @Override
    public void mouseReleased(MouseEvent e) {}
    @Override
    public void mouseExited(MouseEvent e) {}
    @Override
    public void mouseEntered(MouseEvent e) {}
    @Override
    public void mouseClicked(MouseEvent e) {}
    }

class CSTransform
{
    private ImagePlus imp;
    private ImageProcessor ip;
    private int width;
    private int height;
    private int[] lut;
    private String title;

    public CSTransform(ImagePlus image,int[] l,String t)
    {
        imp=image;
        ip=imp.getProcessor();
        width=ip.getWidth();
        height=ip.getHeight();
        lut=l;
        title=t;
    }

    public void setLUT(int[] l) 
    {
        System.arraycopy(l,0,lut,0,256);
    }

    public byte[] applyTransform(byte[] pixels) 
    {
        byte[] output=new byte[pixels.length];

        for (int i=0;i<pixels.length;i++)
            output[i]=(byte) lut[pixels[i] & 0xff];
        return output;
    }

    public int[] applyTransform(int[] pixels) 
    {
        int[] output=new int[pixels.length];

        for (int i=0;i<pixels.length;i++) 
        {
            int c=pixels[i];
            int red=(c & 0xff0000) >> 16;
            int green=(c & 0x00ff00) >> 8;
            int blue=(c & 0x0000ff);
            red=lut[red & 0xff];
            green=lut[green & 0xff];
            blue=lut[blue & 0xff];
            output[i]=((red & 0xff)<<16)+((green & 0xff)<<8)+(blue & 0xff);
        }
        return output;
    }

    public ImagePlus createProcessed8bitImage() 
    {
        ImageProcessor processed_ip=ip.crop();
        ImagePlus processed=imp.createImagePlus();
        processed.setProcessor(title,processed_ip);
        byte[] processed_pixels=(byte[]) processed_ip.getPixels();
        byte[] output=applyTransform(processed_pixels);
        System.arraycopy(output,0,processed_pixels,0,output.length);
        return processed;
    }

    public void createProcessed8bitImage(ImagePlus img) 
    {
        byte[] processed_pixels=(byte[]) (img.getProcessor()).getPixels();
        byte[] output=applyTransform(processed_pixels);
        System.arraycopy(output,0,processed_pixels,0,output.length);
    }

    public ImagePlus createProcessedRGBImage() 
    {
        ImageProcessor processed_ip=ip.crop();
        ImagePlus processed=imp.createImagePlus();
        processed.setProcessor(title,processed_ip);
        int[] processed_pixels=(int[]) processed_ip.getPixels();
        int[] output=applyTransform(processed_pixels);
        System.arraycopy(output,0,processed_pixels,0,output.length);
        return processed;
    }

    public void createProcessedRGBImage(ImagePlus img) 
    {
        int[] processed_pixels=(int[]) (img.getProcessor()).getPixels();
        int[] output=applyTransform(processed_pixels);
        System.arraycopy(output,0,processed_pixels,0,output.length);
    }
}
}