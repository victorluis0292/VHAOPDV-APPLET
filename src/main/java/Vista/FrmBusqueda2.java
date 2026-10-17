package Vista;

import Estilos.Estilos;
import Helper.CobroHelper;
import Modelo.Conexion;
import Modelo.Productos;
import Modelo.ProductosDao;
import static Vista.Sistema.TableVenta;
import static Vista.Sistema.lblEnviaTotal;
import static Vista.Sistema.txtCodigoVenta;
import java.awt.KeyboardFocusManager;
import java.awt.event.KeyEvent;
import java.awt.KeyEventPostProcessor;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import javax.swing.JOptionPane;
import javax.swing.JTable;
import javax.swing.Timer;
import javax.swing.table.DefaultTableModel;
import Vista.VentanaCantidadBusqueda;

/**
 *
 * @author vic
 */
public class FrmBusqueda2 extends javax.swing.JFrame {
    public String origen = "venta"; // declarado arriba
private int idEmpresaActiva;

public void setIdEmpresaActiva(int idEmpresa) {
    this.idEmpresaActiva = idEmpresa;
}
   private Timer timer;
        Connection con;
    Conexion cn = new Conexion();
    PreparedStatement ps;
    ResultSet rs;
    DefaultTableModel modelo = new DefaultTableModel(); 

   Productos pro = new Productos();
    ProductosDao proDao = new ProductosDao();
     String mayus;
      double TotalPagar = 0.00;
    /**
     * Creates new form FrmBusqueda
     */
   public FrmBusqueda2(int idEmpresa, String origen) {
    initComponents();

    this.idEmpresaActiva = idEmpresa; // asignamos la empresa activa
    this.origen = origen; // asignamos el origen de la ventana

    // Aplica estilo a la tabla
    Estilos.estiloTablas(TableProductoJF);
    mostrarEstadoInicial();   // 👈 al abrir, antes de escribir nada

    // Agregar KeyEventPostProcessor para detectar ESCAPE solo una vez
    KeyboardFocusManager kb = KeyboardFocusManager.getCurrentKeyboardFocusManager();
    kb.addKeyEventPostProcessor(new KeyEventPostProcessor() {
        @Override
        public boolean postProcessKeyEvent(KeyEvent e) {
            if (e.getID() == KeyEvent.KEY_PRESSED && e.getKeyCode() == KeyEvent.VK_ESCAPE) {
                dispose();
                txtCodigoVenta.requestFocus();
                return false; // Consume el evento para que no siga
            }
            return true;
        }
    });

    txtBuscar.requestFocus();
    this.setLocationRelativeTo(null);
    txtCantidadVenta.setVisible(false);
    jLabel5.setVisible(false);
    txtcodigo.setVisible(false);
}

   
    @SuppressWarnings("unchecked")
    // <editor-fold defaultstate="collapsed" desc="Generated Code">                          
    private void initComponents() {

        jScrollPane8 = new javax.swing.JScrollPane();
        TableProductoJF = new javax.swing.JTable();
        jLabel5 = new javax.swing.JLabel();
        txtCantidadVenta = new javax.swing.JTextField();
        txtBuscar = new javax.swing.JTextField();
        jLabel52 = new javax.swing.JLabel();
        txtcodigo = new javax.swing.JTextField();
        jPanel1 = new javax.swing.JPanel();

        setDefaultCloseOperation(javax.swing.WindowConstants.DISPOSE_ON_CLOSE);
        setName("formKeyReleased"); // NOI18N
        addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                formKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                formKeyReleased(evt);
            }
        });

        TableProductoJF.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        TableProductoJF.setModel(new javax.swing.table.DefaultTableModel(
            new Object [][] {

            },
            new String [] {
                "ID", "CODIGO", "DESCRIPCIÓN", "CANTIDAD", "PRECIO"
            }
        ) {
            boolean[] canEdit = new boolean [] {
                false, false, false, false, false
            };

            public boolean isCellEditable(int rowIndex, int columnIndex) {
                return canEdit [columnIndex];
            }
        });
        TableProductoJF.setRowHeight(25);
        TableProductoJF.addMouseListener(new java.awt.event.MouseAdapter() {
            public void mouseClicked(java.awt.event.MouseEvent evt) {
                TableProductoJFMouseClicked(evt);
            }
        });
        TableProductoJF.addInputMethodListener(new java.awt.event.InputMethodListener() {
            public void caretPositionChanged(java.awt.event.InputMethodEvent evt) {
                TableProductoJFCaretPositionChanged(evt);
            }
            public void inputMethodTextChanged(java.awt.event.InputMethodEvent evt) {
            }
        });
        TableProductoJF.addPropertyChangeListener(new java.beans.PropertyChangeListener() {
            public void propertyChange(java.beans.PropertyChangeEvent evt) {
                TableProductoJFPropertyChange(evt);
            }
        });
        TableProductoJF.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                TableProductoJFKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                TableProductoJFKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                TableProductoJFKeyTyped(evt);
            }
        });
        jScrollPane8.setViewportView(TableProductoJF);
        if (TableProductoJF.getColumnModel().getColumnCount() > 0) {
            TableProductoJF.getColumnModel().getColumn(0).setMinWidth(10);
            TableProductoJF.getColumnModel().getColumn(0).setPreferredWidth(50);
            TableProductoJF.getColumnModel().getColumn(0).setMaxWidth(50);
            TableProductoJF.getColumnModel().getColumn(1).setMinWidth(50);
            TableProductoJF.getColumnModel().getColumn(1).setPreferredWidth(100);
            TableProductoJF.getColumnModel().getColumn(1).setMaxWidth(60);
            TableProductoJF.getColumnModel().getColumn(2).setResizable(false);
            TableProductoJF.getColumnModel().getColumn(2).setPreferredWidth(200);
            TableProductoJF.getColumnModel().getColumn(3).setMinWidth(20);
            TableProductoJF.getColumnModel().getColumn(3).setPreferredWidth(50);
            TableProductoJF.getColumnModel().getColumn(4).setPreferredWidth(50);
        }

        jLabel5.setFont(new java.awt.Font("Tahoma", 1, 12)); // NOI18N
        jLabel5.setText("Cant");

        txtCantidadVenta.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtCantidadVenta.setText("1");
        txtCantidadVenta.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtCantidadVentaActionPerformed(evt);
            }
        });
        txtCantidadVenta.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtCantidadVentaKeyTyped(evt);
            }
        });

        txtBuscar.setFont(new java.awt.Font("Tahoma", 0, 20)); // NOI18N
        txtBuscar.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtBuscarActionPerformed(evt);
            }
        });
        txtBuscar.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtBuscarKeyPressed(evt);
            }
            public void keyReleased(java.awt.event.KeyEvent evt) {
                txtBuscarKeyReleased(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtBuscarKeyTyped(evt);
            }
        });

        jLabel52.setFont(new java.awt.Font("Tahoma", 1, 18)); // NOI18N
        jLabel52.setForeground(new java.awt.Color(51, 51, 255));
        jLabel52.setText("Buscar ");

        txtcodigo.setFont(new java.awt.Font("Tahoma", 0, 16)); // NOI18N
        txtcodigo.addActionListener(new java.awt.event.ActionListener() {
            public void actionPerformed(java.awt.event.ActionEvent evt) {
                txtcodigoActionPerformed(evt);
            }
        });
        txtcodigo.addKeyListener(new java.awt.event.KeyAdapter() {
            public void keyPressed(java.awt.event.KeyEvent evt) {
                txtcodigoKeyPressed(evt);
            }
            public void keyTyped(java.awt.event.KeyEvent evt) {
                txtcodigoKeyTyped(evt);
            }
        });

        javax.swing.GroupLayout jPanel1Layout = new javax.swing.GroupLayout(jPanel1);
        jPanel1.setLayout(jPanel1Layout);
        jPanel1Layout.setHorizontalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );
        jPanel1Layout.setVerticalGroup(
            jPanel1Layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGap(0, 100, Short.MAX_VALUE)
        );

        javax.swing.GroupLayout layout = new javax.swing.GroupLayout(getContentPane());
        getContentPane().setLayout(layout);
        layout.setHorizontalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(99, 99, 99)
                        .addComponent(jLabel52)
                        .addGap(27, 27, 27)
                        .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 274, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(123, 123, 123)
                        .addComponent(jLabel5)
                        .addGap(33, 33, 33)
                        .addComponent(txtCantidadVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 40, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addGap(58, 58, 58)
                        .addComponent(txtcodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 112, javax.swing.GroupLayout.PREFERRED_SIZE))
                    .addGroup(layout.createSequentialGroup()
                        .addGap(26, 26, 26)
                        .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 936, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addContainerGap(javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE))
        );
        layout.setVerticalGroup(
            layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
            .addGroup(javax.swing.GroupLayout.Alignment.TRAILING, layout.createSequentialGroup()
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap(30, Short.MAX_VALUE)
                        .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.BASELINE)
                            .addComponent(txtBuscar, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                            .addComponent(jLabel52)
                            .addComponent(jLabel5)
                            .addComponent(txtCantidadVenta, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE))
                        .addGap(18, 18, 18))
                    .addGroup(layout.createSequentialGroup()
                        .addContainerGap()
                        .addComponent(txtcodigo, javax.swing.GroupLayout.PREFERRED_SIZE, 30, javax.swing.GroupLayout.PREFERRED_SIZE)
                        .addPreferredGap(javax.swing.LayoutStyle.ComponentPlacement.RELATED, javax.swing.GroupLayout.DEFAULT_SIZE, Short.MAX_VALUE)))
                .addGroup(layout.createParallelGroup(javax.swing.GroupLayout.Alignment.LEADING)
                    .addComponent(jScrollPane8, javax.swing.GroupLayout.PREFERRED_SIZE, 570, javax.swing.GroupLayout.PREFERRED_SIZE)
                    .addGroup(layout.createSequentialGroup()
                        .addGap(15, 15, 15)
                        .addComponent(jPanel1, javax.swing.GroupLayout.PREFERRED_SIZE, javax.swing.GroupLayout.DEFAULT_SIZE, javax.swing.GroupLayout.PREFERRED_SIZE)))
                .addGap(63, 63, 63))
        );

        pack();
    }// </editor-fold>                        

    private void TableProductoJFMouseClicked(java.awt.event.MouseEvent evt) {                                             
     
    }                                            

    private void TableProductoJFKeyPressed(java.awt.event.KeyEvent evt) {                                           

  if(evt.getKeyCode() == KeyEvent.VK_ENTER){
        evt.consume(); // para que no haga la acción por defecto (como cambiar fila)
        EnterBusqueda();
    }

    }                                          

    
       
    
    
    private void txtCantidadVentaActionPerformed(java.awt.event.ActionEvent evt) {                                                 
        // TODO add your handling code here:
    }                                                

    private void txtCantidadVentaKeyPressed(java.awt.event.KeyEvent evt) {                                            
        // TODO add your handling code here:
      
    }                                           

    private void txtCantidadVentaKeyTyped(java.awt.event.KeyEvent evt) {                                          
    
    }                                         

    private void txtBuscarActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         

    private void txtBuscarKeyPressed(java.awt.event.KeyEvent evt) {                                     
  if (evt.getKeyCode() == KeyEvent.VK_DOWN) {
            if (TableProductoJF.getRowCount() > 0) {
                TableProductoJF.requestFocus();
                TableProductoJF.setRowSelectionInterval(0, 0);
            }
        }
    }

    public void EnterBusqueda() {
       int fila = TableProductoJF.getSelectedRow();

    if (fila == -1) {
        JOptionPane.showMessageDialog(null, "No se seleccionó ninguna fila");
        return;
    }

    String codigoSeleccionado = TableProductoJF.getValueAt(fila, 1).toString();

    // 🔍 Consultamos el producto completo para conocer su categoría
    Productos productoCompleto = proDao.BuscarPro(codigoSeleccionado, idEmpresaActiva);

    // 🥬 Si es de Verdulería, saltamos VentanaCantidadBusqueda y vamos
    // directo al panel de detalle con báscula/peso.
    if (productoCompleto != null
            && productoCompleto.getCategoria() != null
            && "verduleria".equalsIgnoreCase(productoCompleto.getCategoria().trim())) {

        Sistema.getInstancia().abrirDetalleVerduleria(productoCompleto);
        dispose(); // cerrar la ventana de búsqueda
        return;
    }

    // 🔁 Flujo normal (no Verdulería): igual que antes
    VentanaCantidadBusqueda consulta = new VentanaCantidadBusqueda();
    consulta.sedDato(codigoSeleccionado);
    consulta.origen = this.origen;
    VentanaCantidadBusqueda.setIdEmpresaActiva(this.idEmpresaActiva);
    consulta.setVisible(true);

    dispose();
    }                                    

    private void txtBuscarKeyReleased(java.awt.event.KeyEvent evt) {                                      
        if (timer != null) {
            timer.stop();
        }

        String textoBusqueda = txtBuscar.getText().trim();
        if (!textoBusqueda.isEmpty()) {
            timer = new Timer(200, e -> listar());
            timer.setRepeats(false);
            timer.start();
        } else {
    DefaultTableModel modeloVacio = new DefaultTableModel();
    modeloVacio.setColumnIdentifiers(new Object[]{"id", "codigo", "nombre", "stock", "precio"});
    TableProductoJF.setModel(modeloVacio);
    mostrarEstadoInicial();   // 👈 al borrar todo el texto
}

        // Al presionar flecha abajo, pasar foco y seleccionar primera fila
        if (evt.getKeyCode() == KeyEvent.VK_DOWN && TableProductoJF.getRowCount() > 0) {
            TableProductoJF.requestFocus();
            TableProductoJF.setRowSelectionInterval(0, 0);
            TableProductoJF.repaint(); // fuerza refresco visual
        }
    }                                     

    private void txtBuscarKeyTyped(java.awt.event.KeyEvent evt) {                                   

    }                                  

    private void TableProductoJFKeyTyped(java.awt.event.KeyEvent evt) {                                         
        
 //if(evt.getKeyCode() == evt.VK_ENTER){
   //      evt.consume();
     //}
int fila = TableProductoJF.getSelectedRow();
      if(fila == -1){
          txtBuscar.requestFocus();
     JOptionPane.showMessageDialog(null,"No se selecciono ningna fila");
        }else{
          // EnterBusqueda();      
        }            // TODO add your handling code here:
  
      
    }                                        
    private final EmptyBusquedaInicial emptyInicial = new EmptyBusquedaInicial();
    private final EmptyBusquedaSinResultados emptySinResultados = new EmptyBusquedaSinResultados();
    private void mostrarEstadoInicial() {
    jScrollPane8.setColumnHeaderView(null);          // oculta el encabezado azul de la tabla
    jScrollPane8.setViewportView(emptyInicial);
    }

    private void mostrarSinResultados(String termino) {
    emptySinResultados.setTermino(termino);
    jScrollPane8.setColumnHeaderView(null);
    jScrollPane8.setViewportView(emptySinResultados);
    }

    private void mostrarTabla() {
    jScrollPane8.setViewportView(TableProductoJF);
    jScrollPane8.setColumnHeaderView(TableProductoJF.getTableHeader());
    }
    
    private void TableProductoJFPropertyChange(java.beans.PropertyChangeEvent evt) {                                               
      // TODO add your handling code here:
    }                                              

    private void TableProductoJFCaretPositionChanged(java.awt.event.InputMethodEvent evt) {                                                     
        // TODO add your handling code here:
    }                                                    

    private void TableProductoJFKeyReleased(java.awt.event.KeyEvent evt) {                                            
       // TODO add your handling code here:
    }                                           

    private void formKeyReleased(java.awt.event.KeyEvent evt) {                                 

              // TODO add your handling code here:
    }                                

    private void formKeyPressed(java.awt.event.KeyEvent evt) {                                
       // TODO add your handling code here:
    }                               

    private void txtcodigoActionPerformed(java.awt.event.ActionEvent evt) {                                          
        // TODO add your handling code here:
    }                                         

    private void txtcodigoKeyPressed(java.awt.event.KeyEvent evt) {                                     
        // TODO add your handling code here:
    }                                    

    private void txtcodigoKeyTyped(java.awt.event.KeyEvent evt) {                                   
        // TODO add your handling code here:
    }                                  

    /**
     * @param args the command line arguments
     */
    public static void main(String args[]) {
      /* Set the Nimbus look and feel */
    try {
        for (javax.swing.UIManager.LookAndFeelInfo info : javax.swing.UIManager.getInstalledLookAndFeels()) {
            if ("Nimbus".equals(info.getName())) {
                javax.swing.UIManager.setLookAndFeel(info.getClassName());
                break;
            }
        }
    } catch (Exception ex) {
        java.util.logging.Logger.getLogger(FrmBusqueda2.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
    }

    /* Create and display the form */
    java.awt.EventQueue.invokeLater(new Runnable() {
        public void run() {
            int idEmpresaPrueba = 1;       // ejemplo de empresa activa
            String origenPrueba = "venta"; // ejemplo de origen
            new FrmBusqueda2(idEmpresaPrueba, origenPrueba).setVisible(true);
        }
    });
    }

  
  //suma las filas de la tabla venta
   public void TotalPagarX() {
        TotalPagar = 0.00;
        int numFila = TableVenta.getRowCount();
        for (int i = 0; i < numFila; i++) {
            double cal = Double.parseDouble(String.valueOf(TableVenta.getModel().getValueAt(i, 4)));
            
            TotalPagar = TotalPagar + cal;
        }
        lblEnviaTotal.setText(String.format("%.2f", TotalPagar));
    }
   
  
    
 
public void listar() {
    DefaultTableModel modelo = new DefaultTableModel();
    Connection nuevaConexion = null;
    PreparedStatement ps = null;
    ResultSet rs = null;

    // Obtener el texto de búsqueda y limpiar espacios
    String textoBusqueda = txtBuscar.getText().trim().toLowerCase().replaceAll("\\s+", "");

    // Query filtrando por id_empresa
    String sql = "SELECT * FROM productos " +
                 "WHERE (CAST(id AS CHAR) LIKE ? " +
                 "OR LOWER(codigo) LIKE ? " +
                 "OR REPLACE(LOWER(nombre), ' ', '') LIKE ? " +
                 "OR precio LIKE ?) " +
                 "AND id_empresa = ?";

    try { Conexion conexion = new Conexion();
        nuevaConexion = conexion.getConnection();

        ps = nuevaConexion.prepareStatement(sql);
        String searchTerm = "%" + textoBusqueda + "%";

        ps.setString(1, searchTerm);
        ps.setString(2, searchTerm);
        ps.setString(3, searchTerm);
        ps.setString(4, searchTerm);
        ps.setInt(5, idEmpresaActiva); // empresa activa

        rs = ps.executeQuery();

        // DEBUG: imprimir valores para verificar
        System.out.println("Buscando: " + textoBusqueda);
        System.out.println("Empresa activa: " + idEmpresaActiva);

        modelo.setColumnIdentifiers(new Object[]{"id", "codigo", "nombre", "stock", "precio"});

        while (rs.next()) {
            System.out.println(rs.getString("id") + " - " + rs.getString("nombre") + " - " + rs.getInt("id_empresa"));
            modelo.addRow(new Object[]{
                rs.getString("id"),
                rs.getString("codigo"),
                rs.getString("nombre"),
                rs.getString("stock"),
                rs.getString("precio")
            });
        }

        TableProductoJF.setModel(modelo);
        TableProductoJF.getColumnModel().getColumn(0).setPreferredWidth(50);
        TableProductoJF.getColumnModel().getColumn(1).setPreferredWidth(100);
        TableProductoJF.getColumnModel().getColumn(2).setPreferredWidth(300);
        TableProductoJF.getColumnModel().getColumn(3).setPreferredWidth(50);
        TableProductoJF.getColumnModel().getColumn(4).setPreferredWidth(70);
        TableProductoJF.setRowHeight(30);
        Estilos.estiloTablas(TableProductoJF);

        if (modelo.getRowCount() == 0) {
        mostrarSinResultados(txtBuscar.getText().trim());   // grillo
        } else {
        mostrarTabla();                                     // resultados
        }
    } catch (Exception e) {
        JOptionPane.showMessageDialog(null, "Error al cargar los productos: " + e.getMessage());
    } finally {
        try {
            if (rs != null) rs.close();
            if (ps != null) ps.close();
            if (nuevaConexion != null && !nuevaConexion.isClosed()) nuevaConexion.close();
        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}

       
    // Variables declaration - do not modify                     
    public static javax.swing.JTable TableProductoJF;
    private javax.swing.JLabel jLabel5;
    private javax.swing.JLabel jLabel52;
    private javax.swing.JPanel jPanel1;
    private javax.swing.JScrollPane jScrollPane8;
    private javax.swing.JTextField txtBuscar;
    private javax.swing.JTextField txtCantidadVenta;
    private javax.swing.JTextField txtcodigo;
    // End of variables declaration                   
}