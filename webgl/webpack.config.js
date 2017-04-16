'use strict';
const path = require('path');
const webpack = require('webpack');

module.exports = function (env) {
  const plugins = [];
  if (env.production) {
    plugins.push(new webpack.optimize.UglifyJsPlugin({
      compress: {
        'dead_code': true,
        conditionals: true,
        comparisons: true,
        evaluate: true,
        booleans: true,
        loops: true,
        unused: true
      },
      beautify: {
        'ascii_only': false
      },
      sourceMap: true
    }));

    plugins.push(new webpack.LoaderOptionsPlugin({
      minimize: true
    }));
  }

  return {
    entry: {
      prediction: './prediction.js',
      collection: './collection.js'
    },
    output: {
      filename: '[name]/[name].js',
      path: path.join(__dirname, 'public')
    },
    module: {
      rules: [{
        test: /\.js$/,
        exclude: /(node_modules)/,
        use: {
          loader: 'babel-loader',
          options: {
            presets: [
              ['env', { modules: false }]
            ]
          }
        }
      }]
    },
    devtool: env.production ? 'cheap-module-source-map' : 'eval',
    devServer: {
      contentBase: path.join(__dirname, 'public'),
      port: 9050
    },
    plugins
  };
};
