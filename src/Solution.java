import java.io.PrintStream;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Scanner;

public class Solution {

	static Solution.Node currentNode;
	static Map<String, Solution.Node> allDirectorys = new HashMap<>();

	static class Node {
		// Variables
		private String data;

		private boolean directory;

		// only if it is directory
		private List<Node> childs;

		private Node parent;

		public Node getParent() {
			return parent;
		}

		public void setParent(Node parent) {
			this.parent = parent;
		}

		public Node(String data, boolean directory, Node parent) {
			super();
			if (directory) {
				data = "/" + data;
				allDirectorys.put(data, this);
			}
			this.data = data;
			this.directory = directory;
			childs = new ArrayList<>();
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

		public List<Node> getChilds() {
			return childs;
		}

		public void setChilds(List<Node> childs) {
			this.childs = childs;
		}

		public void ls(PrintStream out, boolean recursive) {
			if (recursive) {
				this.getChilds().stream().forEach((Node node) -> {
					out.println(node);
					node.ls(out, recursive);
				});
			} else {
				out.print(this.toString());
			}

		}

		public void ls2(PrintStream out, boolean recursive, String param) {
			if (recursive) {
				this.getChilds().stream().forEach((Node node) -> {
					out.print(node);
					node.ls(out, recursive);
				});
			} else {
				if (param.lastIndexOf("/") > -1) {
					String lastDir = param.substring(param.lastIndexOf("/"));
					Solution.Node lastNodeDir = allDirectorys.get("/" + lastDir);
					if (lastNodeDir == null) {
						out.println("Invalid path");
						return;
					}
					lastNodeDir.getChilds().stream().forEach(out::println);
				} else {
					this.getChilds().stream().forEach(out::println);
				}
			}

		}

		public void makeEntry(PrintStream out, String param, boolean b) {
			if (this.getChilds().stream().noneMatch((Node node) -> node.data.equals("/" + param))) {
				this.getChilds().add(new Node(param, b, this));
			} else {
				if (b)
					out.println("Directory already exists");
			}

		}

		public void changeDirectory(String param) {

			currentNode = allDirectorys.containsKey("/" + param) ? allDirectorys.get("/" + param) : currentNode;

		}

		public void changeDirectory2(PrintStream out, String param) {

			String[] splited = param.split("/");

			if (splited.length > 1) {
				//search on entire tree 
				List<String> asList = Arrays.asList(splited);

				if (asList.stream().allMatch((String nodeName) -> allDirectorys.containsKey("/" + nodeName) || "".equals(nodeName) )) {
					if (param.lastIndexOf("/") > -1) {
						String lastDir = param.substring(param.lastIndexOf("/"));
						Solution.Node lastNodeDir = allDirectorys.get(lastDir);
						currentNode = allDirectorys.get(lastNodeDir.data);
					}
				} else {
					out.println("Invalid path");
				}
			} else {
				//only can be childs 
				if (childs.stream().anyMatch((Node node)->
					node.data.equals("/" + param)
						)) {
					currentNode = allDirectorys.containsKey("/" + param) ? allDirectorys.get("/" + param) : currentNode;
				}
			}

		}

		public String pwd(Node parent) {
			if (parent.getParent() == null) {
				return parent.data;
			} else {
				return pwd(parent.parent) + parent.data;
			}
		}

	}

	public static void main(String args[]) throws Exception {

		currentNode = new Node("root", true, null);

		try (Scanner scanner = new Scanner(System.in)) {
			// prompt for the user's name

			while (scanner.hasNext()) {
				String cmd = scanner.nextLine();
				if (cmd.equals("quit"))
					break;

				String[] cmdAndParams = cmd.split(" ");

				handleCmd(cmdAndParams, System.out);

			}
		}
		/* Enter your code here. Read input from STDIN. Print output to STDOUT */
	}

	private static void handleCmd(String[] cmdAndParams, PrintStream out) {
		String param = "";

		switch (cmdAndParams[0]) {
		case "pwd":
			String myPwd = currentNode.pwd(currentNode);
			out.println(myPwd);
			break;
		case "ls":
			String dir = "";
			if (cmdAndParams.length == 2) {
				if ("-r".equalsIgnoreCase(cmdAndParams[1])) {
					param = cmdAndParams[1];
				} else {
					dir = cmdAndParams[1];					
				}
			}
			if (cmdAndParams.length == 3) {
				dir = cmdAndParams[2];
			}
			// print current dir
			out.println(currentNode);
			currentNode.ls2(out, param.equalsIgnoreCase("-r"), dir);
			break;
		case "mkdir":
		case "touch":
			if (cmdAndParams.length > 1) {
				if (cmdAndParams[1].length() > 100) {
					out.println("Invalid File or Folder Name");
				} else {
					param = cmdAndParams[1];
				}
				currentNode.makeEntry(out, param, "mkdir".equals(cmdAndParams[0]));
			} else {
				out.println("Invalid File or Folder Name");
			}
			break;
		case "cd":
			if (cmdAndParams.length > 1) {
				if (cmdAndParams[1].length() > 100) {
					return;
				} else {
					param = cmdAndParams[1];
				}

				currentNode.changeDirectory2(out, param);
			} else {
				// invalid directory
			}
			break;
			default: break;
		}

	}

}
