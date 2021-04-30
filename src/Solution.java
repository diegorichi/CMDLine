import java.io.PrintStream;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.Scanner;
import java.util.function.Function;
import java.util.function.UnaryOperator;

class Node {
	private String data;

	private boolean directory;

	// only if it is directory
	private Map<String, Node> childs;

	private Node parent;

	public Node getParent() {
		return parent;
	}

	public Node(String data, boolean directory, Node parent) {
		super();
		if (directory) {
			data = "/" + data;
		}
		this.data = data;
		this.directory = directory;
		childs = new HashMap<>();
		this.parent = parent;

	}

	@Override
	public String toString() {
		return data;
	}

	public String getData() {
		return data;
	}

	@Override
	public int hashCode() {
		final int prime = 31;
		int result = 1;
		result = prime * result + ((data == null) ? 0 : data.hashCode());
		result = prime * result + (directory ? 1231 : 1237);
		result = prime * result + ((parent == null) ? 0 : parent.hashCode());
		return result;
	}

	@Override
	public boolean equals(Object obj) {
		if (this == obj)
			return true;
		if (obj == null)
			return false;
		if (getClass() != obj.getClass())
			return false;
		Node other = (Node) obj;
		if (data == null) {
			if (other.data != null)
				return false;
		} else if (!data.equals(other.data))
			return false;
		if (directory != other.directory)
			return false;
		if (parent == null) {
			if (other.parent != null)
				return false;
		} else if (!parent.equals(other.parent))
			return false;
		return true;
	}

	public void setData(String data) {
		this.data = data;
	}

	public Map<String, Node> addChild(Node child) {
		childs.put(child.data, child);
		return childs;
	}

	public Map<String, Node> getChilds() {
		return childs;
	}

	public Node getRoot() {
		if (parent == null) {
			return this;
		}
		return parent.getRoot();
	}

	/**
	 * Find directory matching all path in param
	 * 
	 * @param param
	 * @param root
	 * @return
	 */
	public Optional<Node> findDirectory(Node root, String[] param) {
		if (root.data.equals("/" + param[0])) {
			if (param.length > 1) {
				Optional<Node> findedDirectory = root.findDirectory(param[0]);
				if (findedDirectory.isPresent()) {
					String[] temp = new String[param.length - 1];
					System.arraycopy(param, 1, temp, 0, param.length - 1);
					Node item = findedDirectory.get();
					return item.findDirectory(item, temp);
				} else {
					return Optional.empty();
				}
			} else {
				return Optional.of(root);
			}
		}
		return Optional.empty();
	}

	/**
	 * Find directory and return it if present
	 * 
	 * @param param
	 * @return
	 */
	public Optional<Node> findDirectory(String param) {

		return childs.containsKey("/" + param) ? Optional.of(childs.get("/" + param)) : Optional.empty();

	}

}

class Command {
	private Node node;

	Command(Node node) {
		this.node = node;
	}

	public String lsThis() {
		StringBuilder result = new StringBuilder();
		node.getChilds().values().forEach((Node item) -> {
			result.append(item);
			result.append("\n");
		});
		return result.toString();
	}

	public String ls(boolean recursive) {
		StringBuilder result = new StringBuilder();
		result.append(lsThis());
		if (recursive) {
			for (Node item : node.getChilds().values()) {
				result.append(new Command(item).ls(recursive));
			}
		}
		return result.toString();
	}

	public String mkdir(String param) {
		return createNode(param, true);
	}

	public String touch(String param) {
		return createNode(param, false);
	}

	private String createNode(String param, boolean directory) {

		if (param.length() > 100) {
			return "Invalid File or Folder Name";
		}

		if (!node.getChilds().containsKey(directory ? "/" + param : param)) {
			node.addChild(new Node(param, directory, node));
		} else {
			if (directory)
				return "Directory already exists";
		}
		return "";
	}

	public Optional<Node> changeDirectory(String param) {
		// remove initial / to avoid empty entrys
		String[] splited = null;
		if (param.indexOf("/") == 0) {
			param = param.substring(1, param.length());
			splited = param.split("/");
			// find all directory in tree
			Node root = node.getRoot();
			return root.findDirectory(root, splited);
		}

		// only from childs
		return node.findDirectory(param);

	}

	public String pwd(Node parent) {
		if (parent.getParent() == null) {
			return parent.getData();
		} else {
			return pwd(parent.getParent()) + parent.getData();
		}
	}
}

class HandleAndDispatchCMD {

	Map<String, Runnable> commandMap = new HashMap<>();

	Node currentNode;

	PrintStream out;

	
	static HandleAndDispatchCMD instance = null;

	static HandleAndDispatchCMD getInstance() {
		if (instance == null) {
			instance = new HandleAndDispatchCMD();
		}
		return instance;
	}

	public HandleAndDispatchCMD() {
		super();
		currentNode = new Node("root", true, null);
		out = System.out;
	}

	public void handleCmd(String[] cmdAndParams) {

		prepareCommand(cmdAndParams);

		String cmd = cmdAndParams[0];
		if (commandMap.containsKey(cmd)) {
			commandMap.get(cmd).run();
		}else {
			 out.println("Invalid command");
		}
	}

	private void prepareCommand(String[] cmdAndParams) {
		Command command = new Command(currentNode);

		Function<Node, String> pwd = command::pwd;
		Function<Boolean, String> ls = command::ls;
		UnaryOperator<String> mkdir = command::mkdir;
		UnaryOperator<String> touch = command::touch;
		Function<String, Optional<Node>> cd = command::changeDirectory;

		commandMap.put("pwd", () -> out.println(pwd.apply(currentNode)));
		commandMap.put("ls", () -> {
			String dir = "";
			if (cmdAndParams.length == 2) {
				out.println(currentNode);
				dir = ls.apply("-r".equalsIgnoreCase(cmdAndParams[1]));
			} else {
				dir = ls.apply(false);
			}
			out.println(dir);
		});
		commandMap.put("mkdir", () -> out.println(mkdir.apply(cmdAndParams[1])));
		commandMap.put("touch", () -> out.println(touch.apply(cmdAndParams[1])));
		commandMap.put("cd", () -> {
			Optional<Node> optional = cd.apply(cmdAndParams[1]);
			currentNode = optional.orElse(currentNode);
			if (optional.isEmpty())
				out.println("Invalid path");
		});
	}
}

class MainProgram implements Runnable {

	HandleAndDispatchCMD dispatcher = null;

	@Override
	public void run() {

		dispatcher = HandleAndDispatchCMD.getInstance();

		try (Scanner scanner = new Scanner(System.in)) {

			while (scanner.hasNext()) {
				String cmd = scanner.nextLine();
				if (cmd.equals("quit"))
					break;

				dispatcher.handleCmd(cmd.split(" "));

			}
		}
	}

}

public class Solution {

	public static void main(String args[]) throws Exception {

		new Thread(new MainProgram()).start();

	}
}