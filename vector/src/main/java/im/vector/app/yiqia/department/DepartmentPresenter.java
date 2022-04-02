package im.vector.app.yiqia.department;

import java.util.List;

import im.vector.app.yiqia.department.data.IDisplayBean;

/**
 * Created by zhouguanjie on 2019/9/10.
 */
public class DepartmentPresenter implements DepartmentContract.Presenter {

    private DepartmentContract.View mView;

    private DepartmentModel mModel;

    public DepartmentPresenter(DepartmentContract.View view, String departmentId) {
        this.mView = view;
        mModel = new DepartmentModel(this);
        mModel.initData(departmentId);
    }

    @Override
    public void loadData(List<IDisplayBean> departments, boolean isAllUser, int showMembersTagPos) {
        mView.showData(departments, isAllUser, showMembersTagPos);
    }

    @Override
    public void initData(String departmentId) {
        mModel.initData(departmentId);
    }


}
