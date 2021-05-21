package ie.cct.cbwa.Splittr.model;

public class Expense {
	/**
	 * @param name - name of the user who is entering the expense
	 * @param amount - how much is this expense 
	 * @param label - description of this expense
	 */
	private String name;
	private Integer amount;
	private String label;

	public Expense() {
		super();
	}
	
	public Expense(String name, Integer amout, String label) {
		super();
		this.name = name;
		this.amount = amout;
		this.label = label;
	}

	public String getName() {
		return name;
	}
	
	public void setName(String name) {
		this.name = name;
	}
	
	public Integer getAmount() {
		return amount;
	}
	
	public void setAmount(Integer amount) {
		this.amount = amount;
	}
	
	public String getLabel() {
		return label;
	}

	public void setLabel(String label) {
		this.label = label;
	}
}
