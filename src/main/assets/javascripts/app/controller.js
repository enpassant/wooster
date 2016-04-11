define(['./model', 'menu', 'collection', 'mithril'],
    function (model, menu, collection, m)
{
    model.controller = function(data) {
        const params = m.route.param();

        if (params.path) {
            if (params.componentName && model.components[params.componentName]) {
                this.component = model.components[params.componentName];
            }
        } else {
            menu.load('/').then(menu.initToken, model.errorHandler(menu));
        }
    };
});
