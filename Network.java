import javax.swing.*;
import javax.swing.table.TableColumn;
import java.awt.*;
import java.util.HashMap;
import java.util.Map;

/**
 * @Author Jarod Ruschill
 */
public class Network extends JFrame {
    String nHop;
    private JPanel mainPanel, tablePanel, resultPanel;

    private String[][] tableData = {
            {"135.46.56.0/22", "Interface 0"}, {"135.46.60.0/22", "Interface 1"},
            {"192.53.40.0/23", "Router 1"}, {"Default", "Router 2"}
    };
    private String[] columnNames = {"Address/mask", "Next hop"};
    private JTable table = new JTable(tableData, columnNames);
    private TableColumn column1, column2 = null;
    private JLabel ipText, ipResult, nextHopText, nextHopResult;

    public String getNextHop() {
        return nHop;
    }

    public String setNextHop(String hop) {
        return (this.nHop = hop);
    }

    Network() {
        /*
        - takes as an input:
        - a 32 bit IP address
        - a routing table: a list of IP address - subnet mask - Interface (NIC card) pairs
         */
        int octet1, octet2, octet3, octet4;
        String ip;
        octet1 = Integer.parseInt(JOptionPane.showInputDialog("What is the first octet? "));
        octet2 = Integer.parseInt(JOptionPane.showInputDialog("What is the second octet? "));
        octet3 = Integer.parseInt(JOptionPane.showInputDialog("What is the third octet? "));
        octet4 = Integer.parseInt(JOptionPane.showInputDialog("What is the fourth octet? "));
        ip = (octet1 + "." + octet2 + "." + octet3 + "." + octet4);

        int binaryIp = ipToBinary(ip);

        /*
         - does a binary AND of the subnet mask with the input IP address to extract the network part of the address
         */
        Map<String, CIDR> mappings = new HashMap<>();

        for (String[] data : tableData) {
            String cidrString = data[0];

            CIDR cidr;
            if (cidrString.equalsIgnoreCase("default")) {
                // Have the default router represented by a null cidr
                cidr = null;
            } else {
                cidr = parseCidr(data[0]);
            }
            String name = data[1];

            mappings.put(name, cidr);
        }

        /*
        - compares the network part of the address with each address in its routing table
        - if it matches with one of them , it routes the packet to the matching Interface
        - if it matches none of them , it routes the packet to the Default Router (Gateway)
         */

        String nextHop = null;

        for (Map.Entry<String, CIDR> mapping : mappings.entrySet()) {
            String name = mapping.getKey();
            CIDR cidr = mapping.getValue();

            // Skip the default router
            if (cidr == null) continue;

            if (cidr.contains(binaryIp)) {
                nextHop = name;
                break;
            }
        }

        // If no hops were found default to the default router
        if (nextHop == null) {
            for (Map.Entry<String, CIDR> mapping : mappings.entrySet()) {
                if (mapping.getValue() == null) {
                    nextHop = mapping.getKey();
                    break;
                }
            }
        }

        setNextHop(nextHop);

        mainPanel = new JPanel(new BorderLayout());
        resultPanel = new JPanel(new GridLayout(2, 2));
        resultPanel.setBackground(Color.WHITE);
        ipText = new JLabel("IP Address: ");
        ipText.setHorizontalAlignment(JLabel.HORIZONTAL);
        ipText.setFont(new Font(ipText.getFont().toString(), Font.PLAIN, 24));
        resultPanel.add(ipText);
        ipResult = new JLabel("");
        ipResult.setHorizontalAlignment(JLabel.HORIZONTAL);
        ipResult.setForeground(Color.RED);
        ipResult.setFont(new Font(ipText.getFont().toString(), Font.PLAIN, 24));
        resultPanel.add(ipResult);
        nextHopText = new JLabel("Next Hop: ");
        nextHopText.setHorizontalAlignment(JLabel.HORIZONTAL);
        nextHopText.setFont(new Font(ipText.getFont().toString(), Font.PLAIN, 24));
        resultPanel.add(nextHopText);
        nextHopResult = new JLabel("");
        nextHopResult.setHorizontalAlignment(JLabel.HORIZONTAL);
        nextHopResult.setForeground(Color.BLUE);
        nextHopResult.setFont(new Font(ipText.getFont().toString(), Font.PLAIN, 24));
        resultPanel.add(nextHopResult);

        tablePanel = new JPanel(new GridLayout(2, 1));
        column1 = table.getColumnModel().getColumn(0);
        column1.setPreferredWidth(500);
        column2 = table.getColumnModel().getColumn(1);
        column2.setPreferredWidth(500);
        table.setRowHeight(40);
        table.setFont(new Font("Serif", Font.BOLD, 20));
        table.setFillsViewportHeight(true);
        tablePanel.add(table.getTableHeader());
        tablePanel.add(table);
        table.getTableHeader().setForeground(Color.BLUE);
        table.getTableHeader().setFont(new Font(ipText.getFont().toString(), Font.PLAIN, 24));
        mainPanel.add(tablePanel, BorderLayout.PAGE_END);

        JComboBox ipChoice = new JComboBox();
        ipChoice.addItem(ip);

        ipChoice.addActionListener(e -> {
            ipResult.setText((ipChoice.getSelectedItem().toString()));

            if (ip.equals(ipChoice.getSelectedItem().toString())) {
                nextHopResult.setText(getNextHop());

            }
        });

        mainPanel.add(resultPanel);
        mainPanel.add(ipChoice, BorderLayout.NORTH);
        add(mainPanel);
        repaint();
    }

    public static void main(String[] args) {
        Network main = new Network();
        main.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        main.setSize(800, 600);
        main.setVisible(true);
        main.setTitle("Demo");
    }

    /**
     * Converts a ip to a binary number (represented as a int)
     *
     * @param ip the ip
     * @return the binary representation as an int
     */
    private static int ipToBinary(String ip) {
        String[] octets = ip.split("\\.");

        byte[] octetBytes = new byte[4];

        for (int i = 0; i < 4; i++) {
            octetBytes[i] = (byte) Integer.parseInt(octets[i]);
        }

        return ((octetBytes[0] & 0xff) << 24)
                + ((octetBytes[1] & 0xff) << 16)
                + ((octetBytes[2] & 0xff) << 8)
                + (octetBytes[3] & 0xff);
    }

    private static CIDR parseCidr(String cidr) {
        String[] parts = cidr.split("/");

        int ip = ipToBinary(parts[0]);
        byte mask = Byte.parseByte(parts[1]);

        return new CIDR(ip, mask);
    }

    private static class CIDR {

        private int prefix;
        private byte bits;

        public CIDR(int prefix, byte bits) {
            this.prefix = prefix;
            this.bits = bits;
        }

        public boolean contains(int ip) {
            int offsetPrefix = this.prefix >> (32 - bits);
            int offsetIP = ip >> (32 - bits);
            return offsetPrefix == offsetIP;
        }
    }
}