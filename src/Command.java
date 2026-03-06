interface Command {
    void execute();
}

class SaveCommand implements Command {
    private Object data;
    private String file;

    public SaveCommand(Object data, String file) {
        this.data = data;
        this.file = file;
    }

    @Override
    public void execute() {
        DataManager.getInstance().save(data, file);
    }
}