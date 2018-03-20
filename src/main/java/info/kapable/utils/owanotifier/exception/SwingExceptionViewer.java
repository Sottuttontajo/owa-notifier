package info.kapable.utils.owanotifier.exception;

import javax.swing.JOptionPane;

public class SwingExceptionViewer
{
	private String title;

	public SwingExceptionViewer(String title)
	{
		this.title = title;
	}

	public void show(Throwable t)
	{
		JOptionPane.showMessageDialog(null, t.getMessage(), title, JOptionPane.ERROR_MESSAGE);
	}
}